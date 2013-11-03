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

import static org.junit.Assert.assertEquals;

import javax.management.ObjectName;

import org.junit.Test;

import be.fgov.kszbcss.rhq.websphere.connector.ObjectNameMapper;

public class ObjectNameMapperTest {
    @Test
    public void testMapRoutable() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("mycell", "mynode", "myserver");
        ObjectName serverObjectName = new ObjectName("MyDomain:type=SomeMBean,cell=mycell,node=mynode,process=myserver");
        ObjectName clientObjectName = mapper.toClientObjectName(serverObjectName);
        assertEquals(new ObjectName("MyDomain:type=SomeMBean"), clientObjectName);
        assertEquals(serverObjectName, mapper.toServerObjectName(clientObjectName));
    }
    
    @Test
    public void testMapNonRoutable() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("mycell", "mynode", "myserver");
        ObjectName name = new ObjectName("MyDomain:type=SomeMBean,name=test");
        assertEquals(name, mapper.toClientObjectName(name));
        assertEquals(name, mapper.toServerObjectName(name));
    }
    
    @Test
    public void testToServerObjectNameWithPropertyPattern() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("mycell", "mynode", "myserver");
        ObjectName name = new ObjectName("MyDomain:type=MyMBean,*");
        assertEquals(name, mapper.toServerObjectName(name));
    }
}
