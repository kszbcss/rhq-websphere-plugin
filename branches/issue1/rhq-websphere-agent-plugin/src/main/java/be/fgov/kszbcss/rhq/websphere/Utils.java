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
package be.fgov.kszbcss.rhq.websphere;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.event.Event;
import org.rhq.core.pluginapi.event.EventContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;

public class Utils {
    private Utils() {}
    
    public static ObjectName createObjectName(String s) {
        try {
            return new ObjectName(s);
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    public static MBeanStatDescriptor getMBeanStatDescriptor(EmsBean bean, String... path) {
        try {
            ObjectName mbean = new ObjectName(bean.getBeanName().toString());
            return path.length == 0 ? new MBeanStatDescriptor(mbean) : new MBeanStatDescriptor(mbean, new StatDescriptor(path));
        } catch (JMException ex) {
            throw new EmsException(ex);
        }
    }
    
    public static List<Element> getElements(Element parent, String localName) {
        List<Element> result = new ArrayList<Element>();
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getLocalName().equals(localName)) {
                result.add((Element)child);
            }
            child = child.getNextSibling();
        }
        return result;
    }
    
    public static Element getFirstElement(Element parent, String localName) {
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getLocalName().equals(localName)) {
                return (Element)child;
            }
            child = child.getNextSibling();
        }
        return null;
    }
    
    private static final Object eventPublishLock = new Object();
    
    // TODO: this is necessary because EventManager#publishEvents doesn't correctly
    //       synchronize access to the EventReport; this causes corruption of the
    //       report, which will eventually result in an HTTP 500 error from the server
    public static void publishEvent(EventContext context, Event event) {
        synchronized (eventPublishLock) {
            context.publishEvent(event);
        }
    }
}
