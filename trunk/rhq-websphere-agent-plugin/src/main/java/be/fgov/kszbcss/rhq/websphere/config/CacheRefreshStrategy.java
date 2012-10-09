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
package be.fgov.kszbcss.rhq.websphere.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheRefreshStrategy {
    private static final Log log = LogFactory.getLog(CacheRefreshStrategy.class);
    
    private static final ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>();
    
    static void setImmediateRefresh(Boolean immediateRefresh) {
        if ((immediateRefresh == null) == (threadLocal.get() == null)) {
            throw new IllegalStateException();
        }
        if (log.isDebugEnabled()) {
            log.debug("Setting immediateRefresh to " + immediateRefresh);
        }
        threadLocal.set(immediateRefresh);
    }
    
    public static boolean isImmediateRefresh() {
        Boolean immediateRefresh = threadLocal.get();
        if (immediateRefresh == null) {
            log.warn("No cache refresh strategy defined in current context; using default");
            return true;
        } else {
            return immediateRefresh;
        }
    }
}
