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
package be.fgov.kszbcss.rhq.websphere.connector.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingTrustManager implements X509TrustManager {
    private static final Logger log = LoggerFactory.getLogger(DelegatingTrustManager.class);
    
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            TrustStoreManager.getInstance().getTrustManager().checkServerTrusted(chain, authType);
            log.info("Accepted server certificate for " + chain[0].getSubjectDN());
        } catch (CertificateException ex) {
            log.error("Rejected server certificate for " + chain[0].getSubjectDN() + ": " + ex.getMessage());
            throw ex;
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return TrustStoreManager.getInstance().getTrustManager().getAcceptedIssuers();
    }
}
