/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
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
package be.fgov.kszbcss.rhq.websphere.config;

import java.util.Collection;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

final class RelativePath<T extends ConfigObject> extends Path<T> {
    private final Path<?> parent;
    private final Class<T> type;
    private final String path;
    
    RelativePath(Path<?> parent, Class<T> type, String name) {
        this.parent = parent;
        this.type = type;
        path = ConfigObjectTypeRegistry.getDescriptor(type).getName() + "=" + name;
    }

    @Override
    Class<T> getType() {
        return type;
    }

    @Override
    <S extends ConfigObject> Collection<S> resolveRelative(String relativePath, Class<S> type) throws JMException, ConnectorException, InterruptedException {
        if (relativePath == null) {
            return parent.resolveRelative(path, type);
        } else {
            return parent.resolveRelative(path + ":" + relativePath, type);
        }
    }
}
