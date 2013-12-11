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
package be.fgov.kszbcss.rhq.websphere.connector;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminClientStats {
    private final Map<String,AdminClientStatsData> map = new HashMap<String,AdminClientStatsData>();
    private final Date beginTime;
    private Date endTime;
    
    public AdminClientStats() {
        beginTime = new Date();
    }
    
    public void addData(String destination, long nanos) {
        AdminClientStatsData data;
        synchronized (map) {
            data = map.get(destination);
            if (data == null) {
                data = new AdminClientStatsData(destination);
                map.put(destination, data);
            }
        }
        data.addData(nanos);
    }
    
    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Collection<AdminClientStatsData> getData() {
        return map.values();
    }
    
    public void end() {
        endTime = new Date();
    }
}
