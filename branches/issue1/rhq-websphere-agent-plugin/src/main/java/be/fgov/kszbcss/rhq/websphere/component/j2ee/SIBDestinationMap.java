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

import java.io.Serializable;
import java.util.Map;

/**
 * Maps JNDI names to SIB destinations.
 */
public class SIBDestinationMap implements Serializable {
    private static final long serialVersionUID = 8757662576395051692L;
    
    private final Map<String,SIBDestination> map;
    
    SIBDestinationMap(Map<String,SIBDestination> map) {
        this.map = map;
    }
    
    /**
     * Get the SIB destination for a given JNDI name;
     * 
     * @param jndiName
     *            the JNDI name
     * @return the information about the SIB destination or <code>null</code> if the JNDI name is
     *         not bound to a SIB destination
     */
    public SIBDestination getSIBDestination(String jndiName) {
        return map.get(jndiName);
    }
}
