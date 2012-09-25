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

import java.util.Arrays;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class StatsCollectingAdminClient extends AdminClientWrapper {
    private final AdminClientStatsCollector collector;
    
    public StatsCollectingAdminClient(AdminClient target, AdminClientStatsCollector collector) {
        super(target);
        this.collector = collector;
    }
    
    private StringBuilder formatObjectName(ObjectName objectName) {
        StringBuilder builder = new StringBuilder();
        builder.append(objectName.getDomain());
        builder.append(':');
        String type = objectName.getKeyProperty("type");
        builder.append(type == null ? "<unknown>" : type);
        return builder;
    }
    
    @Override
    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException, ConnectorException {
        long start = System.nanoTime();
        try {
            return super.getAttribute(name, attribute);
        } finally {
            long time = System.nanoTime()-start;
            collector.addData(formatObjectName(name).append('@').append(attribute).toString(), time);
        }
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException,
            ConnectorException {
        long start = System.nanoTime();
        try {
            return super.getAttributes(name, attributes);
        } finally {
            long time = System.nanoTime()-start;
            StringBuilder destination = formatObjectName(name);
            if (attributes.length == 1) {
                destination.append('@');
                destination.append(attributes[0]);
            } else {
                // EMS frequently requests multiple attributes from a single MBean. However, the list of attributes
                // is not sorted and the order of attributes doesn't seem to be deterministic. To produce meaningful
                // statistics, we sort the attributes names.
                String[] sortedAttributes = attributes.clone();
                Arrays.sort(sortedAttributes);
                destination.append("@{");
                for (int i=0; i<sortedAttributes.length; i++) {
                    if (i > 0) {
                        destination.append(',');
                    }
                    destination.append(sortedAttributes[i]);
                }
                destination.append('}');
            }
            collector.addData(destination.toString(), time);
        }
    }

    @Override
    public Object invoke(ObjectName name, String operationName,
            Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, ConnectorException {
        long start = System.nanoTime();
        try {
            return super.invoke(name, operationName, params, signature);
        } finally {
            long time = System.nanoTime()-start;
            collector.addData(formatObjectName(name).append('#').append(operationName).toString(), time);
        }
    }
}
