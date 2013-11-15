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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains the configuration of a deployed application.
 */
public class ApplicationConfiguration implements Serializable {
    private static final long serialVersionUID = 3962483806663765684L;

    private final Map<String,List<Map<String,String>>> data;

    public ApplicationConfiguration(Map<String,List<Map<String,String>>> data) {
        this.data = data;
    }

    public Map<String,List<Map<String,String>>> getData() {
        return data;
    }
    
    public List<Map<String,String>> getData(String task, String module, String bean) {
        List<Map<String,String>> orgList = data.get(task);
        if (orgList == null) {
            return null;
        } else {
            List<Map<String,String>> filteredList = null;
            for (Map<String,String> entry : orgList) {
                // The data also has a column called "module", but it contains the display name of the
                // module, which is not what we use to identify a module. We extract the module name from
                // the uri, which is "<name>.war,WEB-INF/web.xml" or "<name>.jar,META-INF/ejb-jar.xml".
                String uri = entry.get("uri");
                int idx = uri.indexOf(',');
                if (module.equals(uri.substring(0, idx)) && (bean == null || bean.equals(entry.get("EJB")))) {
                    if (filteredList == null) {
                        filteredList = new ArrayList<Map<String,String>>();
                    }
                    filteredList.add(entry);
                }
            }
            return filteredList;
        }
    }
}
