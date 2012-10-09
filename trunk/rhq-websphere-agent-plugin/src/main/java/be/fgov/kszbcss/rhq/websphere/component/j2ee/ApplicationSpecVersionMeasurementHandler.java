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
package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.regex.Pattern;

import org.w3c.dom.Document;

import com.ibm.websphere.management.exception.ConnectorException;

public class ApplicationSpecVersionMeasurementHandler extends SpecVersionMeasurementHandler {
    private static final Pattern publicIdPattern = Pattern.compile("-//Sun Microsystems, Inc\\.//DTD J2EE Application ([0-9]\\.[0-9])//EN");
    
    private final ApplicationComponent applicationComponent;
    
    public ApplicationSpecVersionMeasurementHandler(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    @Override
    protected Document getDeploymentDescriptor() throws InterruptedException, ConnectorException {
        return applicationComponent.getApplicationInfo().getDeploymentDescriptor();
    }

    @Override
    protected Pattern[] getPublicIdPatterns() {
        return new Pattern[] { publicIdPattern };
    }
}
