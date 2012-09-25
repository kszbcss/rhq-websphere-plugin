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

public class SIBDestination implements Serializable {
    private static final long serialVersionUID = -5897283441401077236L;

    private final String busName;
    private final String destinationName;
    
    public SIBDestination(String busName, String destinationName) {
        this.busName = busName;
        this.destinationName = destinationName;
    }

    public String getBusName() {
        return busName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public String toString() {
        return busName + ":" + destinationName;
    }
}
