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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementHandler;

public abstract class SpecVersionMeasurementHandler implements MeasurementHandler {
    private static final Log log = LogFactory.getLog(SpecVersionMeasurementHandler.class);
    
    protected abstract Document getDeploymentDescriptor() throws InterruptedException, ConnectorException;
    protected abstract Pattern[] getPublicIdPatterns();
    
    public final void getValue(WebSphereServer server, MeasurementReport report, MeasurementScheduleRequest request) throws InterruptedException, ConnectorException {
        Document document = getDeploymentDescriptor();
        if (document != null) {
            DocumentType docType = document.getDoctype();
            String version = null;
            if (docType != null) {
                String publicId = docType.getPublicId();
                if (log.isDebugEnabled()) {
                    log.debug("Public ID: " + publicId);
                }
                for (Pattern pattern : getPublicIdPatterns()) {
                    Matcher matcher = pattern.matcher(publicId);
                    if (matcher.matches()) {
                        version = matcher.group(1);
                        if (log.isDebugEnabled()) {
                            log.debug("Public ID matches pattern; version=" + version);
                        }
                        break;
                    }
                }
                if (version == null) {
                    log.warn("Unexpected public ID found in application.xml deployment descriptor: " + publicId);
                }
            }
            if (version == null) {
                version = document.getDocumentElement().getAttribute("version");
            }
            report.addData(new MeasurementDataTrait(request, version));
        }
    }
}
