/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.connector.agent;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.measurement.calltime.CallTimeData;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import be.fgov.kszbcss.rhq.websphere.ConfigurationBasedProcessLocator;
import be.fgov.kszbcss.rhq.websphere.DeploymentManager;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStats;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsCollector;
import be.fgov.kszbcss.rhq.websphere.connector.AdminClientStatsData;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreAction;
import be.fgov.kszbcss.rhq.websphere.connector.security.TrustStoreManager;

public class ConnectorSubsystemComponent implements ResourceComponent<ResourceComponent<?>>, MeasurementFacet, OperationFacet {
    private static final Log log = LogFactory.getLog(ConnectorSubsystemComponent.class);
    
    public void start(ResourceContext<ResourceComponent<?>> context)
            throws InvalidPluginConfigurationException, Exception {
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

    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException, Exception {
        if (name.equals("retrieveCellCertificate")) {
            DeploymentManager dm = new DeploymentManager(null, new ConfigurationBasedProcessLocator(parameters));
            String cell = dm.getCell();
            ConfigQueryService configQueryService = ConfigQueryServiceFactory.getInstance().getConfigQueryServiceWithoutCaching(dm);
            try {
                X509Certificate cert = configQueryService.query(CellRootCertificateQuery.INSTANCE);
                TrustStoreManager.getInstance().addCertificate("cell:" + cell, cert);
            } finally {
                configQueryService.release();
            }
            return null;
        } else if (name.equals("retrieveCertificateFromPort")) {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(new KeyManager[0],
                    new TrustManager[] { new AutoImportTrustManager(parameters.getSimple("alias").getStringValue())},
                    new SecureRandom());
            SSLSocket socket = (SSLSocket)sslContext.getSocketFactory().createSocket(
                    parameters.getSimple("host").getStringValue(), parameters.getSimple("port").getIntegerValue());
            try {
                socket.startHandshake();
            } finally {
                socket.close();
            }
            return null;
        } else if (name.equals("listCertificates")) {
            final PropertyList certificates = new PropertyList("certificates");
            TrustStoreManager.getInstance().execute(new TrustStoreAction() {
                public void execute(KeyStore truststore) throws Exception {
                    // Sort the aliases for convenience
                    Set<String> aliases = new TreeSet<String>();
                    for (Enumeration<String> e = truststore.aliases(); e.hasMoreElements(); ) {
                        aliases.add(e.nextElement());
                    }
                    for (String alias : aliases) {
                        X509Certificate cert = (X509Certificate)truststore.getCertificate(alias);
                        PropertyMap map = new PropertyMap("certificate");
                        map.put(new PropertySimple("alias", alias));
                        map.put(new PropertySimple("subject", cert.getSubjectDN().toString()));
                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                        md.update(cert.getEncoded());
                        byte[] digest = md.digest();
                        StringBuilder fingerprint = new StringBuilder();
                        for (int i = 0; i < digest.length; i++) {
                            if (i > 0) {
                                fingerprint.append(':');
                            }
                            fingerprint.append(getHexDigit(((int)digest[i] & 0xf0) >> 4));
                            fingerprint.append(getHexDigit((int)digest[i] & 0x0f));
                        }
                        map.put(new PropertySimple("fingerprint", fingerprint.toString()));
                        certificates.add(map);
                    }
                }
            }, true);
            if (log.isDebugEnabled()) {
                log.debug("certificates=" + certificates);
            }
            OperationResult result = new OperationResult();
            result.getComplexResults().put(certificates);
            return result;
        } else if (name.equals("removeCertificate")) {
            final String alias = parameters.getSimple("alias").getStringValue();
            TrustStoreManager.getInstance().execute(new TrustStoreAction() {
                public void execute(KeyStore truststore) throws Exception {
                    truststore.deleteEntry(alias);
                }
            }, false);
            return null;
        } else if (name.equals("renameCertificate")) {
            final String oldAlias = parameters.getSimple("oldAlias").getStringValue();
            final String newAlias = parameters.getSimple("newAlias").getStringValue();
            TrustStoreManager.getInstance().execute(new TrustStoreAction() {
                public void execute(KeyStore truststore) throws Exception {
                    Certificate cert = truststore.getCertificate(oldAlias);
                    truststore.setCertificateEntry(newAlias, cert);
                    truststore.deleteEntry(oldAlias);
                }
            }, false);
            return null;
        } else {
            return null;
        }
    }
    
    static char getHexDigit(int nibble) {
        return (char)(nibble < 10 ? '0' + nibble : 'A' + nibble - 10);
    }

    public void stop() {
    }
}
