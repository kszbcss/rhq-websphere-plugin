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
package be.fgov.kszbcss.rhq.websphere.component.server.log.xm4was;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LogMessageTest {
    @Test
    public void testDecoding() {
        LogMessage message = new LogMessage("[1332841749344:900:1332851446269:test.TestListener:test_context_war:test_context.war:]Test warning");
        assertEquals(1332841749344L, message.getSequence());
        assertEquals(900, message.getLevel());
        assertEquals(1332851446269L, message.getTimestamp());
        assertEquals("test.TestListener", message.getLoggerName());
        assertEquals("test_context_war", message.getApplicationName());
        assertEquals("test_context.war", message.getModuleName());
        assertNull(message.getComponentName());
        assertEquals("Test warning", message.getMessage());
    }
}
