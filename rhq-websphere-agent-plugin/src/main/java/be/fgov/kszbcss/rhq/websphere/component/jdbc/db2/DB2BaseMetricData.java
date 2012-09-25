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
package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DB2BaseMetricData {
    private static final Log log = LogFactory.getLog(DB2BaseMetricData.class);
    
    /**
     * Contains the individual metric values for each DB2 agent.
     */
    private final Map<Long,Long> values = new HashMap<Long,Long>();
    
    /**
     * Sum of the last known values for disconnected agents.
     */
    private long disconnectedSum;
    
    public void addValue(long agentId, long value) {
        values.put(agentId, value);
    }
    
    /**
     * Updates this object with the current metric data. In particular, this method will check for
     * agents that have been disconnected and make sure that the last known values for these agents
     * are taken into account when calculating the sum over all agents.
     * 
     * @param newData
     *            the new data for this metric
     */
    public void update(DB2BaseMetricData newData) {
        Set<Long> newAgents = new HashSet<Long>(newData.values.keySet());
        for (Iterator<Map.Entry<Long,Long>> it = values.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long,Long> entry = it.next();
            Long agentId = entry.getKey();
            Long newValue = newData.values.get(agentId);
            if (newValue == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No metric value for agent with ID " + agentId + "; considering it as disconnected");
                }
                disconnectedSum += entry.getValue();
                it.remove();
            } else {
                entry.setValue(newValue);
            }
            newAgents.remove(agentId);
        }
        for (Long agentId : newAgents) {
            if (log.isDebugEnabled()) {
                log.debug("Got metric value from previously unknown agent with ID " + agentId);
            }
            values.put(agentId, newData.values.get(agentId));
        }
    }
    
    /**
     * Get the sum of the values for this metric over all DB2 agents, including disconnected ones.
     * 
     * @return the sum
     */
    public long getSum() {
        long sum = disconnectedSum;
        for (long value : values.values()) {
            sum += value;
        }
        return sum;
    }
}
