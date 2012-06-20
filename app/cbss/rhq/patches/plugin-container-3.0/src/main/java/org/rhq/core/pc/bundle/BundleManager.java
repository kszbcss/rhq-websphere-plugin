/*
 * RHQ Management
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.core.pc.bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.rhq.core.clientapi.agent.PluginContainerException;
import org.rhq.core.clientapi.agent.bundle.BundleAgentService;
import org.rhq.core.clientapi.agent.bundle.BundleScheduleRequest;
import org.rhq.core.clientapi.agent.bundle.BundleScheduleResponse;
import org.rhq.core.clientapi.server.bundle.BundleServerService;
import org.rhq.core.domain.bundle.BundleDeployment;
import org.rhq.core.domain.bundle.BundleDeploymentStatus;
import org.rhq.core.domain.bundle.BundleResourceDeployment;
import org.rhq.core.domain.bundle.BundleResourceDeploymentHistory;
import org.rhq.core.domain.bundle.BundleType;
import org.rhq.core.domain.bundle.BundleVersion;
import org.rhq.core.domain.content.PackageVersion;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pc.ContainerService;
import org.rhq.core.pc.PluginContainer;
import org.rhq.core.pc.PluginContainerConfiguration;
import org.rhq.core.pc.agent.AgentService;
import org.rhq.core.pc.inventory.InventoryManager;
import org.rhq.core.pc.inventory.ResourceContainer;
import org.rhq.core.pc.util.ComponentUtil;
import org.rhq.core.pc.util.FacetLockType;
import org.rhq.core.pc.util.LoggingThreadFactory;
import org.rhq.core.pluginapi.bundle.BundleDeployRequest;
import org.rhq.core.pluginapi.bundle.BundleDeployResult;
import org.rhq.core.pluginapi.bundle.BundleFacet;
import org.rhq.core.pluginapi.bundle.BundleManagerProvider;
import org.rhq.core.util.MessageDigestGenerator;
import org.rhq.core.util.exception.ThrowableUtil;

/**
 * Manages the bundle subsystem, which allows bundles of content to be installed. 
 *
 * <p>This is an agent service; its interface is made remotely accessible if this is deployed within the agent.</p>
 *
 * @author John Mazzitelli
 */
public class BundleManager extends AgentService implements BundleAgentService, BundleManagerProvider, ContainerService {
    private final Log log = LogFactory.getLog(BundleManager.class);

    private final String AUDIT_DEPLOYMENT_ENDED = "Deployment Ended";
    private final String AUDIT_DEPLOYMENT_STARTED = "Deployment Started";
    private final String AUDIT_DEPLOYMENT_SCHEDULED = "Deployment Scheduled";
    private final String AUDIT_FILE_DOWNLOAD_ENDED = "File Download Started";
    private final String AUDIT_FILE_DOWNLOAD_STARTED = "File Download Started";

    private PluginContainerConfiguration configuration;
    private ExecutorService deployerThreadPool;

    public BundleManager() {
        super(BundleAgentService.class);
    }

    public void setConfiguration(PluginContainerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void initialize() {
        createDeployerThreadPool();
    }

    public void shutdown() {
        shutdownDeployerThreadPool();
    }

    private void createDeployerThreadPool() {
        shutdownDeployerThreadPool(); // paranoia - just in case somehow an old one is still around
        LoggingThreadFactory threadFactory = new LoggingThreadFactory("BundleDeployment", true);
        this.deployerThreadPool = Executors.newSingleThreadExecutor(threadFactory); // single-threaded so only one deployment at a time
    }

    private void shutdownDeployerThreadPool() {
        if (this.deployerThreadPool != null) {
            this.deployerThreadPool.shutdown(); // let it finish what it was doing, so we don't abort in the middle of a deployment
            this.deployerThreadPool = null;
        }
        return;
    }

    public List<PackageVersion> getAllBundleVersionPackageVersions(BundleVersion bundleVersion) throws Exception {
        int bvId = bundleVersion.getId();
        List<PackageVersion> pvs = getBundleServerService().getAllBundleVersionPackageVersions(bvId);
        return pvs;
    }

    public long getFileContent(PackageVersion packageVersion, OutputStream outputStream) throws Exception {
        outputStream = remoteOutputStream(outputStream);
        long size = getBundleServerService().downloadPackageBits(packageVersion, outputStream);
        return size;
    }

    public BundleScheduleResponse schedule(final BundleScheduleRequest request) {
        final BundleScheduleResponse response = new BundleScheduleResponse();

        try {
            final BundleResourceDeployment resourceDeployment = request.getBundleResourceDeployment();
            final BundleDeployment bundleDeployment = resourceDeployment.getBundleDeployment();

            // find the resource that will handle the bundle processing
            InventoryManager im = PluginContainer.getInstance().getInventoryManager();
            BundleType bundleType = bundleDeployment.getBundleVersion().getBundle().getBundleType();
            ResourceType resourceType = bundleType.getResourceType();
            Set<Resource> resources = im.getResourcesWithType(resourceType);
            if (resources.isEmpty()) {
                throw new Exception("No bundle plugin supports bundle type [" + bundleType + "]");
            }
            final int bundleHandlerResourceId = resources.iterator().next().getId();
            final ResourceContainer resourceContainer = im.getResourceContainer(bundleHandlerResourceId);
            if (null == resourceContainer.getResourceContext()) {
                throw new Exception("No bundle plugin resource available to handle deployment for bundle type ["
                    + bundleType
                    + "]. Ensure the bundle plugin is deployed and its resource is imported into inventory.");
            }

            auditDeployment(resourceDeployment, AUDIT_DEPLOYMENT_SCHEDULED, bundleDeployment.getName(),
                "Scheduled deployment time: " + request.getRequestedDeployTimeAsString());

            Runnable deployerRunnable = new Runnable() {
                public void run() {
                    try {
                        // pull down the bundle files that the plugin will need in order to process the bundle
                        File pluginTmpDir = resourceContainer.getResourceContext().getTemporaryDirectory();
                        File bundleFilesDir = new File(pluginTmpDir, "bundle-versions/"
                            + bundleDeployment.getBundleVersion().getId());
                        Map<PackageVersion, File> downloadedFiles = downloadBundleFiles(resourceDeployment,
                            bundleFilesDir);

                        // deploy the bundle utilizing the bundle facet object
                        String deploymentMessage = "Deployment [" + bundleDeployment + "] to ["
                            + resourceDeployment.getResource() + "]";
                        auditDeployment(resourceDeployment, AUDIT_DEPLOYMENT_STARTED, bundleDeployment.getName(),
                            deploymentMessage);

                        BundleDeployRequest deployRequest = new BundleDeployRequest();
                        deployRequest.setBundleManagerProvider(BundleManager.this);
                        deployRequest.setResourceDeployment(resourceDeployment);
                        deployRequest.setBundleFilesLocation(bundleFilesDir);
                        deployRequest.setPackageVersionFiles(downloadedFiles);
                        deployRequest.setCleanDeployment(request.isCleanDeployment());
                        deployRequest.setRevert(request.isRevert());

                        // get the bundle facet object that will process the bundle and call it to start the deployment
                        int facetMethodTimeout = 4 * 60 * 60 * 1000; // 4 hours is given to the bundle plugin to do its thing
                        BundleFacet bundlePluginComponent = getBundleFacet(bundleHandlerResourceId, facetMethodTimeout);
                        BundleDeployResult result = bundlePluginComponent.deployBundle(deployRequest);
                        if (result.isSuccess()) {
                            completeDeployment(resourceDeployment, BundleDeploymentStatus.SUCCESS, deploymentMessage);
                        } else {
                            completeDeployment(resourceDeployment, BundleDeploymentStatus.FAILURE, result
                                .getErrorMessage());
                        }
                    } catch (InterruptedException ie) {
                        log.error("Failed to complete bundle deployment due to interrupt", ie);
                        completeDeployment(resourceDeployment, BundleDeploymentStatus.FAILURE, "Deployment interrupted");
                    } catch (Throwable t) {
                        log.error("Failed to complete bundle deployment", t);
                        completeDeployment(resourceDeployment, BundleDeploymentStatus.FAILURE, "Deployment failed: "
                            + ThrowableUtil.getAllMessages(t));
                    }
                }
            };

            this.deployerThreadPool.execute(deployerRunnable);
        } catch (Throwable t) {
            log.error("Failed to schedule bundle request: " + request, t);
            response.setErrorMessage(t);
        }

        return response;
    }

    /**
     * convenience method:<br/>
     * category defaults to null<br/>
     * status defaults to SUCCESS<br/>
     * attachment defaults null <br/>
     *
     * @param bundleResourceDeployment not null
     * @param action not null
     * @param info not null
     * @param message
     */
    public void auditDeployment(BundleResourceDeployment bundleResourceDeployment, String action, String info,
        String message) {
        auditDeployment(bundleResourceDeployment, action, info, null, BundleResourceDeploymentHistory.Status.SUCCESS,
            message, null);
    }

    public void auditDeployment(BundleResourceDeployment bundleResourceDeployment, String action, String info,
        BundleResourceDeploymentHistory.Category category, BundleResourceDeploymentHistory.Status status,
        String message, String attachment) {
        if (null == action || null == info) {
            throw new IllegalArgumentException("action or info is null");
        }

        if (null == status) {
            status = BundleResourceDeploymentHistory.Status.SUCCESS;
        }
        BundleResourceDeploymentHistory history = new BundleResourceDeploymentHistory("Bundle Plugin", action, info,
            category, status, message, attachment);
        log.debug("Reporting deployment step [" + history + "] to Server...");
        getBundleServerService().addDeploymentHistory(bundleResourceDeployment.getId(), history);
    }

    /**
     * Downloads the bundle's files into the bundle plugin's tmp directory and returns that tmp directory. 
     * 
     * @param resourceDeployment access to deployment information, including what bundle files need to be downloaded
     * @param downloadDir location where the bundle files should be downloaded
     * @return map of the package versions to their files that were downloaded
     * @throws Exception
     */
    private Map<PackageVersion, File> downloadBundleFiles(BundleResourceDeployment resourceDeployment, File downloadDir)
        throws Exception {

        BundleDeployment bundleDeployment = resourceDeployment.getBundleDeployment();
        BundleVersion bundleVersion = bundleDeployment.getBundleVersion();

        Map<PackageVersion, File> packageVersionFiles = new HashMap<PackageVersion, File>();
        List<PackageVersion> packageVersions = getAllBundleVersionPackageVersions(bundleVersion);
        for (PackageVersion packageVersion : packageVersions) {
            File packageFile = new File(downloadDir, packageVersion.getFileName());

            try {
                verifyHash(packageVersion, packageFile);
            } catch (Exception e) {

                // file either doesn't exist or it hash doesn't match, download a new copy
                packageFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(packageFile);
                try {
                    auditDeployment(resourceDeployment, AUDIT_FILE_DOWNLOAD_STARTED, packageVersion.getDisplayName(),
                        "Downloading [" + packageVersion + "]");

                    long size = getFileContent(packageVersion, fos);

                    if (packageVersion.getFileSize() != null && size != packageVersion.getFileSize().longValue()) {
                        String message = "Downloaded bundle file [" + packageVersion + "] but its size was [" + size
                            + "] when it was expected to be [" + packageVersion.getFileSize() + "].";
                        log.warn(message);
                        auditDeployment(resourceDeployment, AUDIT_FILE_DOWNLOAD_ENDED, packageVersion.getDisplayName(),
                            null, BundleResourceDeploymentHistory.Status.WARN, message, null);
                    } else {
                        auditDeployment(resourceDeployment, AUDIT_FILE_DOWNLOAD_ENDED, packageVersion.getDisplayName(),
                            "Download complete for [" + packageVersion + "]");
                    }
                } catch (Exception e2) {
                    String message = "Failed to downloaded bundle file [" + packageVersion + "] " + e2;
                    log.warn(message);
                    auditDeployment(resourceDeployment, AUDIT_FILE_DOWNLOAD_ENDED, packageVersion.getDisplayName(),
                        null, BundleResourceDeploymentHistory.Status.FAILURE, message, null);
                } finally {
                    fos.close();
                }

                // now try to verify it again, if this throws an exception, that is very bad and we need to abort
                verifyHash(packageVersion, packageFile);
            }

            packageVersionFiles.put(packageVersion, packageFile);
        }

        return packageVersionFiles;
    }

    private void completeDeployment(BundleResourceDeployment resourceDeployment, BundleDeploymentStatus status,
        String message) {
        getBundleServerService().setBundleDeploymentStatus(resourceDeployment.getId(), status);
        BundleResourceDeploymentHistory.Status auditStatus = BundleDeploymentStatus.SUCCESS.equals(status) ? BundleResourceDeploymentHistory.Status.SUCCESS
            : BundleResourceDeploymentHistory.Status.FAILURE;
        auditDeployment(resourceDeployment, AUDIT_DEPLOYMENT_ENDED, resourceDeployment.getBundleDeployment().getName(),
            null, auditStatus, message, null);
    }

    /**
     * Checks to see if the package file's hash matches that of the given package version.
     * If the file doesn't exist or the hash doesn't match, an exception is thrown.
     * This method returns normally if the hash matches the file.
     * If there is no known hash in the package version, this method returns normally.
     * 
     * @param packageVersion contains the hash that is expected
     * @param packageFile the local file whose hash is to be checked
     * @throws Exception if the file does not match the hash or the file doesn't exist
     */
    private void verifyHash(PackageVersion packageVersion, File packageFile) throws Exception {
        if (!packageFile.exists()) {
            throw new Exception("Package version [" + packageVersion + "] does not exist, cannot check hash");
        }

        String realHash;
        if (packageVersion.getMD5() != null) {
            realHash = new MessageDigestGenerator(MessageDigestGenerator.MD5).calcDigestString(packageFile);
            if (!packageVersion.getMD5().equals(realHash)) {
                throw new Exception("Package version [" + packageVersion + "] failed MD5 check. expected=["
                    + packageVersion.getMD5() + "], actual=[" + realHash + "]");
            }
        } else if (packageVersion.getSHA256() != null) {
            realHash = new MessageDigestGenerator(MessageDigestGenerator.SHA_256).calcDigestString(packageFile);
            if (!packageVersion.getSHA256().equals(realHash)) {
                throw new Exception("Package version [" + packageVersion + "] failed SHA256 check. expected=["
                    + packageVersion.getSHA256() + "], actual=[" + realHash + "]");
            }
        } else {
            log.debug("Package version [" + packageVersion + "] has no MD5/SHA256 hash - not verifying it");
        }

        return;
    }

    /**
     * If this manager can talk to a server-side {@link BundleServerService}, a proxy to that service is returned.
     *
     * @return the server-side proxy; <code>null</code> if this manager doesn't have a server to talk to
     */
    private BundleServerService getBundleServerService() {
        if (configuration.getServerServices() != null) {
            return configuration.getServerServices().getBundleServerService();
        }

        throw new IllegalStateException("There is no bundle server service available to obtain bundle files");
    }

    /**
     * Given a resource, this obtains that resource's {@link BundleFacet} interface.
     * If the resource does not support that facet, an exception is thrown.
     * The resource must be in the STARTED (i.e. connected) state.
     *
     * @param  resourceId identifies the resource that is to perform the bundle activities
     * @param  timeout    if any facet method invocation thread has not completed after this many milliseconds, interrupt
     *                    it; value must be positive
     *
     * @return the resource's bundle facet interface
     *
     * @throws PluginContainerException on error
     */
    private BundleFacet getBundleFacet(int resourceId, long timeout) throws PluginContainerException {
        return ComponentUtil.getComponent(resourceId, BundleFacet.class, FacetLockType.READ, timeout, false, true);
    }
}
