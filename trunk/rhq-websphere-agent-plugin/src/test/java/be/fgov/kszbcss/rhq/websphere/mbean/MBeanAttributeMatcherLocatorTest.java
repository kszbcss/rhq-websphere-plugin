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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.management.ObjectName;

import org.junit.Test;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanAttributeMatcherLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanLocator;

public class MBeanAttributeMatcherLocatorTest {
    @Test
    public void testEquals() throws Exception {
        MBeanLocator locator1 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        MBeanLocator locator2 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        assertTrue(locator1.equals(locator2));
    }

    @Test
    public void testHashCode() throws Exception {
        MBeanLocator locator1 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        MBeanLocator locator2 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        assertEquals(locator1.hashCode(), locator2.hashCode());
    }
}
