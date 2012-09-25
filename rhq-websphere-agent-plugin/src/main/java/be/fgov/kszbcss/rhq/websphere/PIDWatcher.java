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
package be.fgov.kszbcss.rhq.websphere;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;

public class PIDWatcher {
    private static final Log log = LogFactory.getLog(PIDWatcher.class);
    
    private final AdminClient adminClient;
    private final List<WeakReference<PIDChangeTracker>> trackers = new LinkedList<WeakReference<PIDChangeTracker>>();
    private long lastCheck;
    private String lastKnownPid;

    public PIDWatcher(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public synchronized PIDChangeTracker createTracker() {
        PIDChangeTracker tracker = new PIDChangeTracker(this);
        trackers.add(new WeakReference<PIDChangeTracker>(tracker));
        return tracker;
    }
    
    synchronized void update() {
        long time = System.currentTimeMillis();
        if (time - lastCheck >= 60000L) {
            try {
                String currentPid = (String)adminClient.getAttribute(adminClient.getServerMBean(), "pid");
                if (lastKnownPid == null) {
                    lastKnownPid = currentPid;
                } else if (!currentPid.equals(lastKnownPid)) {
                    if (log.isDebugEnabled()) {
                        log.debug("PID change detected (old=" + lastKnownPid + "; new=" + currentPid + "); informing trackers");
                    }
                    synchronized (trackers) {
                        for (Iterator<WeakReference<PIDChangeTracker>> it = trackers.iterator(); it.hasNext(); ) {
                            PIDChangeTracker tracker = it.next().get();
                            if (tracker == null) {
                                it.remove();
                            } else {
                                tracker.setRestarted();
                            }
                        }
                    }
                    lastKnownPid = currentPid;
                }
            } catch (Exception ex) {
                log.debug("Cannot get PID", ex);
            }
            lastCheck = time;
        }
    }
}
