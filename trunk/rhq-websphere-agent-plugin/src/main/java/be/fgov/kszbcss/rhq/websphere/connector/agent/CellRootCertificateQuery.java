/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;

import com.ibm.websphere.management.exception.ConnectorException;

public class CellRootCertificateQuery implements ConfigQuery<X509Certificate> {
    public static final CellRootCertificateQuery INSTANCE = new CellRootCertificateQuery();
    
    private static final long serialVersionUID = -2297752052572075867L;

    private CellRootCertificateQuery() {}
    
    public X509Certificate execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        for (ConfigObject keyStoreConfig : config.cell().path("Security").resolveSingle().getChildren("keyStores")) {
            if (keyStoreConfig.getAttribute("name").equals("CellDefaultTrustStore")) {
                String location = (String)keyStoreConfig.getAttribute("location");
                if (!location.startsWith("${CONFIG_ROOT}/")) {
                    throw new ConfigQueryException("Cannot extract cell default trust store because it is not located in the configuration repository");
                }
                byte[] cellDefaultTrustStore = config.extract(location.substring(location.indexOf('/')+1));
                X509Certificate cert;
                try {
                    KeyStore ks = KeyStore.getInstance((String)keyStoreConfig.getAttribute("type"), (String)keyStoreConfig.getAttribute("provider"));
                    ks.load(new ByteArrayInputStream(cellDefaultTrustStore), ((String)keyStoreConfig.getAttribute("password")).toCharArray());
                    cert = (X509Certificate)ks.getCertificate("root");
                } catch (GeneralSecurityException ex) {
                    throw new ConfigQueryException("Failed to extract certificate: " + ex.getMessage());
                } catch (IOException ex) {
                    // Since we use a ByteArrayInputStream, we should never get here
                    throw new RuntimeException(ex);
                }
                if (cert == null) {
                    throw new ConfigQueryException("Root certificate not found in CellDefaultTrustStore");
                }
                return cert;
            }
        }
        throw new ConfigQueryException("CellDefaultTrustStore not found");
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CellRootCertificateQuery;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
