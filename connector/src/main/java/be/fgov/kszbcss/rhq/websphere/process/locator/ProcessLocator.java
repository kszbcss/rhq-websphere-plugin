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
package be.fgov.kszbcss.rhq.websphere.process.locator;

import java.util.Properties;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientProvider;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class ProcessLocator implements AdminClientProvider {
    private static final Log log = LogFactory.getLog(ProcessLocator.class);
    
    public final AdminClient createAdminClient() throws ConnectorException {
        Properties properties = new Properties();
        
        try {
            getAdminClientProperties(properties);
        } catch (JMException ex) {
            // TODO: define a proper exception for this
            throw new ConnectorException(ex);
        }
        
        // From the IBM site: "When you use the createAdminClient method within application code that
        // runs on an application server, such as within servlets and JavaServer Pages (JSP) files,
        // you must set the CACHE_DISABLED property to true." Since we use multiple threads and access
        // multiple servers, we assume that this also applies to us.
        properties.setProperty(AdminClient.CACHE_DISABLED, "true");
    
        if (log.isDebugEnabled()) {
            log.debug("Creating AdminClient with properties: " + properties);
        }
        
        return AdminClientFactory.createAdminClient(properties);
    }

    public abstract void getAdminClientProperties(Properties properties) throws JMException, ConnectorException;
}
