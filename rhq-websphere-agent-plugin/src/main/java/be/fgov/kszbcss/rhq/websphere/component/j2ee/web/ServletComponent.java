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
package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.J2EEComponent;

import com.ibm.websphere.pmi.PmiConstants;

public class ServletComponent extends J2EEComponent<WebModuleComponent> implements MeasurementFacet {
    @Override
    protected String getPMIModule() {
        return PmiConstants.WEBAPP_MODULE;
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.SERVLET_SUBMODULE;
    }

    @Override
    protected boolean isConfigured() throws Exception {
        ResourceContext<WebModuleComponent> context = getResourceContext();
        return context.getParentResourceComponent().getServletNames().contains(context.getResourceKey());
    }

    protected AvailabilityType doGetAvailability() {
        // The MBean representing the servlet is registered lazily (or not at all if the application is
        // configured with "Create MBeans for resources" disabled). Therefore the only check we can do is
        // to see if the servlet is declared in the deployment descriptor, which is done in the
        // isConfigured method.
        return AvailabilityType.UP;
    }
}
