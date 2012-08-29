package be.fgov.kszbcss.rhq.websphere.connector.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.content.PackageDetailsKey;
import org.rhq.core.domain.content.PackageType;
import org.rhq.core.domain.content.transfer.ContentResponseResult;
import org.rhq.core.domain.content.transfer.DeployIndividualPackageResponse;
import org.rhq.core.domain.content.transfer.DeployPackageStep;
import org.rhq.core.domain.content.transfer.DeployPackagesResponse;
import org.rhq.core.domain.content.transfer.RemovePackagesResponse;
import org.rhq.core.domain.content.transfer.ResourcePackageDetails;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.measurement.calltime.CallTimeData;
import org.rhq.core.pluginapi.content.ContentFacet;
import org.rhq.core.pluginapi.content.ContentServices;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.cert.util.CertContentConstants;
import be.fgov.kszbcss.rhq.cert.util.CertContentUtils;
import be.fgov.kszbcss.rhq.websphere.ConfigurationBasedProcessLocator;
import be.fgov.kszbcss.rhq.websphere.DeploymentManager;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStats;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsData;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreAction;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreManager;

public class ConnectorSubsystemComponent implements ResourceComponent<ResourceComponent<?>>, MeasurementFacet, ContentFacet, OperationFacet {
    private static final Log log = LogFactory.getLog(ConnectorSubsystemComponent.class);
    
    private ResourceContext<ResourceComponent<?>> resourceContext;
    
    public void start(ResourceContext<ResourceComponent<?>> context)
            throws InvalidPluginConfigurationException, Exception {
        resourceContext = context;
    }

    public AvailabilityType getAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        log.debug("Entering getValues");
        boolean dataAdded = false;
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            if (name.equals("InvocationTime")) {
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
            } else if (name.equals("LogEventsPublished")) {
                report.addData(new MeasurementDataNumeric(request, Double.valueOf(EventStats.getLogEventsPublished())));
            } else if (name.equals("LogEventsSuppressed")) {
                report.addData(new MeasurementDataNumeric(request, Double.valueOf(EventStats.getLogEventsSuppressed())));
            }
        }
        if (!dataAdded) {
            log.debug("No call time data requested. Nothing has been added to the report.");
        }
    }

    public DeployPackagesResponse deployPackages(final Set<ResourcePackageDetails> packages, final ContentServices contentServices) {
        final DeployPackagesResponse response = new DeployPackagesResponse();
        response.setOverallRequestResult(ContentResponseResult.SUCCESS);
        try {
            TrustStoreManager.getInstance().execute(new TrustStoreAction() {
                public void execute(KeyStore truststore) throws Exception {
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
                }
            }, false);
        } catch (Exception ex) {
            response.setOverallRequestResult(ContentResponseResult.FAILURE);
            response.setOverallRequestErrorMessage(ex.getMessage());
        }
        return response;
    }

    public Set<ResourcePackageDetails> discoverDeployedPackages(PackageType packageType) {
        final Set<ResourcePackageDetails> result = new HashSet<ResourcePackageDetails>();
        try {
            TrustStoreManager.getInstance().execute(new TrustStoreAction() {
                public void execute(KeyStore truststore) throws Exception {
                    for (Enumeration<String> aliases = truststore.aliases(); aliases.hasMoreElements(); ) {
                        X509Certificate cert = (X509Certificate)truststore.getCertificate(aliases.nextElement());
                        result.add(new ResourcePackageDetails(new PackageDetailsKey(CertContentUtils.getPackageName(cert),
                                CertContentUtils.getVersion(cert), CertContentConstants.PACKAGE_TYPE_NAME,
                                CertContentConstants.ARCHITECTURE_NAME)));
                    }
                }
            }, true);
        } catch (Exception ex) {
            // Just continue and return an empty result
            log.error("Failed to read trust store", ex);
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

    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        DeploymentManager dm = new DeploymentManager(null, new ConfigurationBasedProcessLocator(parameters));
        final String cell = dm.getCell();
        ConfigQueryService configQueryService = ConfigQueryServiceFactory.getInstance().getConfigQueryService(dm);
        try {
            final X509Certificate cert = configQueryService.query(CellRootCertificateQuery.INSTANCE, true);
            TrustStoreManager.getInstance().execute(new TrustStoreAction() {
                public void execute(KeyStore truststore) throws Exception {
                    truststore.setCertificateEntry("cell:" + cell, cert);
                }
            }, false);
        } finally {
            configQueryService.release();
        }
        return null;
    }

    public void stop() {
    }
}
