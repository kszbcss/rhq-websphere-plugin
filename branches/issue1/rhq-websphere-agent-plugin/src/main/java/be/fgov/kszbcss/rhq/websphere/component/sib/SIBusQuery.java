/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2013 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBQueue;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBus;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBusQuery implements ConfigQuery<SIBusInfo> {
    private static final long serialVersionUID = 1L;
    
    private final String name;

    public SIBusQuery(String name) {
        this.name = name;
    }

    public SIBusInfo execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        config.cell().path(SIBus.class, name).path(SIBQueue.class).resolve();
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SIBusQuery && ((SIBusQuery)obj).name.equals(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + ")";
    }
}
