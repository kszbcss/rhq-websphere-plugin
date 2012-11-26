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

import java.util.Hashtable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;

class NamedThreadFactory implements ThreadFactory, Thread.UncaughtExceptionHandler {
    private static final Log log = LogFactory.getLog(NamedThreadFactory.class);
    
    private final ThreadGroup group;
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    NamedThreadFactory(String namePrefix) {
        group = Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public Thread newThread(final Runnable runnable) {
        // The log4j MDC is stored in an InheritableThreadLocal. We need to clear it.
        Runnable runnableWrapper = new Runnable() {
            public void run() {
                Hashtable<?,?> context = MDC.getContext();
                if (context != null) {
                    context.clear();
                }
                runnable.run();
            }
        };
        Thread t = new Thread(group, runnableWrapper, namePrefix + "-" + threadNumber.getAndIncrement());
        t.setDaemon(false);
        t.setUncaughtExceptionHandler(this);
        return t;
    }

    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception on scheduled thread [" + t.getName() + "]", e);
    }
}
