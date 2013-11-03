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
package be.fgov.kszbcss.rhq.websphere.component.server.log;

import org.apache.commons.lang.ObjectUtils;

/**
 * Identifies a J2EE application component (servlet or enterprise bean). This class is used when
 * dispatching log events to individual components. Note that servlet context listeners are not
 * considered as components, although they can produce log events. They are represented using an
 * instance with a null component name.
 */
public class J2EEComponentKey {
    private final String applicationName;
    private final String moduleName;
    private final String componentName;
    
    public J2EEComponentKey(String applicationName, String moduleName, String componentName) {
        if (applicationName == null || moduleName == null) {
            throw new IllegalArgumentException("applicationName and moduleName must not be null");
        }
        this.applicationName = applicationName;
        this.moduleName = moduleName;
        this.componentName = componentName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof J2EEComponentKey) {
            J2EEComponentKey other = (J2EEComponentKey)obj;
            return applicationName.equals(other.applicationName) && moduleName.equals(other.moduleName) && ObjectUtils.equals(componentName, componentName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31*31*applicationName.hashCode() + 31*moduleName.hashCode() + (componentName == null ? 0 : componentName.hashCode());
    }
}
