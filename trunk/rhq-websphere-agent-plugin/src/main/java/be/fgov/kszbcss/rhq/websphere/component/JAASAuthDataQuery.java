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
package be.fgov.kszbcss.rhq.websphere.component;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

import com.ibm.websphere.management.exception.ConnectorException;

public class JAASAuthDataQuery implements ConfigQuery<JAASAuthDataMap> {
    private static final long serialVersionUID = -7960159720107221635L;

    public JAASAuthDataMap execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        Map<String,JAASAuthData> map = new HashMap<String,JAASAuthData>();
        for (ConfigObject co : config.path("JAASAuthData").resolve()) {
            map.put((String)co.getAttribute("alias"), new JAASAuthData((String)co.getAttribute("userId"), (String)co.getAttribute("password")));
        }
        return new JAASAuthDataMap(map);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JAASAuthDataQuery;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
