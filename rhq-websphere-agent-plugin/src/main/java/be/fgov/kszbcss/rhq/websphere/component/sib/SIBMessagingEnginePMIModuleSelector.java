/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2014 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Arrays;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.stat.StatDescriptor;

import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

/**
 * Calculates a PMI module path relative to a given SIB messaging engine. This class is necessary
 * because the PMI module names for SIB were changed by PM60540.
 */
public class SIBMessagingEnginePMIModuleSelector implements PMIModuleSelector {
    private static final Log log = LogFactory.getLog(SIBMessagingEnginePMIModuleSelector.class);
    
    private final SIBMessagingEngineComponent sibMessagingEngine;
    private String[] relativePath;

    public SIBMessagingEnginePMIModuleSelector(SIBMessagingEngineComponent sibMessagingEngine, String... relativePath) {
        this.sibMessagingEngine = sibMessagingEngine;
        this.relativePath = relativePath;
    }

    public String[] getPath() throws JMException, ConnectorException, InterruptedException {
        String[] path = new String[3+relativePath.length];
        StatDescriptor[] descs = sibMessagingEngine.getServer().listStatMembers(new StatDescriptor(new String[0]), false);
        if (log.isDebugEnabled()) {
            log.debug("Resolving module path relative to messaging engine; descriptors: " + Arrays.asList(descs));
        }
        for (StatDescriptor desc : descs) {
            String name = desc.getPath()[0];
            if (name.equals("SIB Service")) {
                log.debug("Server doesn't have \"fix\" for PM60540");
                path[0] = "SIB Service";
                path[1] = "SIB Messaging Engines";
            } else {
                log.debug("Server has \"fix\" for PM60540");
                path[0] = "StatGroup.SIBService";
                path[1] = "StatGroup.MessagingEngines";
            }
        }
        path[2] = sibMessagingEngine.getName();
        System.arraycopy(relativePath, 0, path, 3, relativePath.length);
        if (log.isDebugEnabled()) {
            log.debug("Resulting StatDescriptor path: " + Arrays.asList(path));
        }
        return path;
    }
}
