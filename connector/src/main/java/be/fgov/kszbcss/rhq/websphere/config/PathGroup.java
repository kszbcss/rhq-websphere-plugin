/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2014 Crossroads Bank for Social Security
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

final class PathGroup<T extends ConfigObject> extends Path<T> {
    private final Class<T> type;
    private final Collection<Path<? extends T>> paths;
    
    PathGroup(Class<T> type, Collection<Path<? extends T>> paths) {
        this.type = type;
        this.paths = paths;
    }

    @Override
    Class<T> getType() {
        return type;
    }

    @Override
    <S extends ConfigObject> Collection<S> resolveRelative(String relativePath, Class<S> type) throws JMException, ConnectorException, InterruptedException {
        List<S> result = new ArrayList<S>();
        for (Path<? extends T> path : paths) {
            result.addAll(path.resolveRelative(relativePath, type));
        }
        return result;
    }
}
