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
package be.fgov.kszbcss.rhq.websphere.mbean;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.process.WebSphereServer;

import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Locates an MBean based on an object name pattern and an attribute value to match.
 */
public class MBeanAttributeMatcherLocator implements MBeanLocator {
    private final MBeanLocator parent;
    private final String attributeName;
    private final String attributeValue;
    
    public MBeanAttributeMatcherLocator(MBeanLocator parent,
            String attributeName, String attributeValue) {
        this.parent = parent;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public MBeanAttributeMatcherLocator(ObjectName pattern,
            String attributeName, String attributeValue) {
        this(new StaticMBeanObjectNamePatternLocator(pattern), attributeName, attributeValue);
    }

    public Set<ObjectName> queryNames(WebSphereServer server) throws JMException, ConnectorException, InterruptedException {
        Set<ObjectName> result = new HashSet<ObjectName>();
        for (ObjectName objectName : parent.queryNames(server)) {
            if (server.getAdminClient().getAttribute(objectName, attributeName).equals(attributeValue)) {
                result.add(objectName);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return parent + "[" + attributeName + "=" + attributeValue + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MBeanAttributeMatcherLocator) {
            MBeanAttributeMatcherLocator other = (MBeanAttributeMatcherLocator)obj;
            return parent.equals(other.parent) && attributeName.equals(other.attributeName) && attributeValue.equals(other.attributeValue);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31*result + attributeName.hashCode();
        result = 31*result + attributeValue.hashCode();
        return result;
    }
}
