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
package be.fgov.kszbcss.rhq.websphere.connector;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class SecureAdminClientProvider implements AdminClientProvider {
    private static final Log log = LogFactory.getLog(SecureAdminClientProvider.class);
    
    private final AdminClientProvider parent;

    public SecureAdminClientProvider(AdminClientProvider parent) {
        this.parent = parent;
    }

    public AdminClient createAdminClient() throws ConnectorException {
        AdminClient adminClient = parent.createAdminClient();
        try {
            Subject subject = WSSubject.getRunAsSubject();
            if (log.isDebugEnabled()) {
                log.debug("Subject = " + subject);
            }
            if (subject != null) {
                WSSubject.setRunAsSubject(null);
                return new SecureAdminClient(adminClient, subject);
            } else {
                return adminClient;
            }
        } catch (WSSecurityException ex) {
            throw new ConnectorException(ex);
        }
    }
}
