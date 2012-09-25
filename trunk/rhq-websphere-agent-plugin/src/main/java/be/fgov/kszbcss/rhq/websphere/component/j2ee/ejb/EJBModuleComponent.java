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
package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;

public class EJBModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "EJBModule";
    }
    
    public Set<String> getBeanNames(EnterpriseBeanType type, boolean immediate) throws InterruptedException, ConnectorException {
        Set<String> names = new HashSet<String>();
        Element beans = Utils.getFirstElement(getModuleInfo(immediate).getDeploymentDescriptor().getDocumentElement(), "enterprise-beans");
        if (beans != null) {
            if (type == EnterpriseBeanType.MESSAGE_DRIVEN) {
                for (Element bean : Utils.getElements(beans, "message-driven")) {
                    names.add(Utils.getFirstElement(bean, "ejb-name").getTextContent());
                }
            } else {
                String typeString = type == EnterpriseBeanType.STATEFUL_SESSION ? "Stateful" : "Stateless";
                for (Element bean : Utils.getElements(beans, "session")) {
                    Element sessionType = Utils.getFirstElement(bean, "session-type");
                    if (sessionType == null || sessionType.getTextContent().equals(typeString)) {
                        names.add(Utils.getFirstElement(bean, "ejb-name").getTextContent());
                    }
                }
            }
        }
        return names;
    }
}
