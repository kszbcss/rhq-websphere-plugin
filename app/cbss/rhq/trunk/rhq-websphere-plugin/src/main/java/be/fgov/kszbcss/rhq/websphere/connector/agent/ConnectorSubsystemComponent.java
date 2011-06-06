package be.fgov.kszbcss.rhq.websphere.connector.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.content.PackageDetailsKey;
import org.rhq.core.domain.content.PackageType;
import org.rhq.core.domain.content.transfer.ContentResponseResult;
import org.rhq.core.domain.content.transfer.DeployIndividualPackageResponse;
import org.rhq.core.domain.content.transfer.DeployPackageStep;
import org.rhq.core.domain.content.transfer.DeployPackagesResponse;
import org.rhq.core.domain.content.transfer.RemovePackagesResponse;
import org.rhq.core.domain.content.transfer.ResourcePackageDetails;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.measurement.calltime.CallTimeData;
import org.rhq.core.pluginapi.content.ContentFacet;
import org.rhq.core.pluginapi.content.ContentServices;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.cert.util.CertContentConstants;
import be.fgov.kszbcss.rhq.cert.util.CertContentUtils;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStats;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsData;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreManager;

public class ConnectorSubsystemComponent implements ResourceComponent<ResourceComponent<?>>, MeasurementFacet, ContentFacet {
    private static final Log log = LogFactory.getLog(ConnectorSubsystemComponent.class);
    
    private ResourceContext<ResourceComponent<?>> resourceContext;
    private File truststoreFile;
    
    public void start(ResourceContext<ResourceComponent<?>> context)
            throws InvalidPluginConfigurationException, Exception {
        resourceContext = context;
        truststoreFile = TrustStoreManager.getInstance().getTruststoreFile();
    }

    public AvailabilityType getAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        log.debug("Entering getValues");
        boolean dataAdded = false;
        for (MeasurementScheduleRequest request : requests) {
            if (request.getName().equals("InvocationTime")) {
                AdminClientStats stats = AdminClientStatsCollector.INSTANCE.rotateStats();
                CallTimeData data = new CallTimeData(request);
                for (AdminClientStatsData statsData : stats.getData()) {
                    data.addAggregatedCallData(statsData.getDestination(), stats.getBeginTime(), stats.getEndTime(),
                            statsData.getMin(), statsData.getMax(), statsData.getTotal(), statsData.getCount());
                }
                report.addData(data);
                if (log.isDebugEnabled()) {
                    log.debug("Added " + data.getValues().size() + " call time data items to the report");
                }
                dataAdded = true;
            }
        }
        if (!dataAdded) {
            log.debug("No call time data requested. Nothing has been added to the report.");
        }
    }

    public DeployPackagesResponse deployPackages(Set<ResourcePackageDetails> packages, ContentServices contentServices) {
        DeployPackagesResponse response = new DeployPackagesResponse();
        response.setOverallRequestResult(ContentResponseResult.SUCCESS);
        try {
            KeyStore truststore = KeyStore.getInstance("JKS");
            if (truststoreFile.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("Loading existing trust store from " + truststoreFile);
                }
                InputStream in = new FileInputStream(truststoreFile);
                try {
                    truststore.load(in, new char[0]);
                } finally {
                    in.close();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Trust store has " + truststore.size() + " existing entries");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Trust store " + truststoreFile + " doesn't exist yet; will create a new one");
                }
                truststore.load(null);
            }
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (ResourcePackageDetails pkg : packages) {
                baos.reset();
                DeployIndividualPackageResponse packageResponse = new DeployIndividualPackageResponse(pkg.getKey());
                packageResponse.setResult(ContentResponseResult.SUCCESS);
                try {
                    long size = contentServices.downloadPackageBits(resourceContext.getContentContext(), pkg.getKey(), baos, true);
                    if (log.isDebugEnabled()) {
                        log.debug("Downloaded package content; size = " + size);
                    }
                    Collection<? extends Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(baos.toByteArray()));
                    for (Certificate cert : certs) {
                        String alias = pkg.getFileName() + "#" + pkg.getVersion();
                        if (log.isDebugEnabled()) {
                            log.debug("Adding certificate for " + ((X509Certificate)cert).getSubjectDN() + " with alias " + alias);
                        }
                        truststore.setCertificateEntry(alias, cert);
                    }
                } catch (Exception ex) {
                    log.error("Failed to add certificate from " + pkg.getFileName(), ex);
                    packageResponse.setResult(ContentResponseResult.FAILURE);
                    packageResponse.setErrorMessage(ex.getMessage());
                    response.setOverallRequestResult(ContentResponseResult.FAILURE);
                    response.setOverallRequestErrorMessage("Deployment of at least one certificate failed");
                }
                response.addPackageResponse(packageResponse);
            }
            if (log.isDebugEnabled()) {
                log.debug("Writing trust store with " + truststore.size() + " entries to " + truststoreFile);
            }
            OutputStream out = new FileOutputStream(truststoreFile);
            try {
                truststore.store(out, new char[0]);
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            response.setOverallRequestResult(ContentResponseResult.FAILURE);
            response.setOverallRequestErrorMessage(ex.getMessage());
        }
        return response;
    }

    public Set<ResourcePackageDetails> discoverDeployedPackages(PackageType packageType) {
        Set<ResourcePackageDetails> result = new HashSet<ResourcePackageDetails>();
        if (truststoreFile.exists()) {
            try {
                KeyStore truststore = KeyStore.getInstance("JKS");
                InputStream in = new FileInputStream(truststoreFile);
                try {
                    truststore.load(in, new char[0]);
                } finally {
                    in.close();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Loaded trust store " + truststoreFile + " with " + truststore.size() + " entries; building package list");
                }
                for (Enumeration<String> aliases = truststore.aliases(); aliases.hasMoreElements(); ) {
                    X509Certificate cert = (X509Certificate)truststore.getCertificate(aliases.nextElement());
                    result.add(new ResourcePackageDetails(new PackageDetailsKey(CertContentUtils.getPackageName(cert),
                            CertContentUtils.getVersion(cert), CertContentConstants.PACKAGE_TYPE_NAME,
                            CertContentConstants.ARCHITECTURE_NAME)));
                }
            } catch (Exception ex) {
                // Just continue and return an empty result
                log.error("Failed to read trust store " + truststoreFile, ex);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Trust store " + truststoreFile + " doesn't exist; returning empty result");
            }
        }
        return result;
    }

    public List<DeployPackageStep> generateInstallationSteps(ResourcePackageDetails arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public RemovePackagesResponse removePackages(
            Set<ResourcePackageDetails> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream retrievePackageBits(ResourcePackageDetails arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void stop() {
    }
}
