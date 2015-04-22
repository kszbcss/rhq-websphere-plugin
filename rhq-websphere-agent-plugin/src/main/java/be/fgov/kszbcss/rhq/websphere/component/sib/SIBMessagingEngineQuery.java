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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.kszbcss.rhq.websphere.config.Config;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBDestination;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBGatewayLinkCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBLocalizationPointCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBMessagingEngineCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBQualifiedDestinationName;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBQueue;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBQueueLocalizationPointCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBTopicSpace;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBTopicSpaceLocalizationPointCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBus;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBMessagingEngineQuery implements ConfigQuery<SIBMessagingEngineInfo> {
    private static final long serialVersionUID = 4L;
    
    private static final Logger log = LoggerFactory.getLogger(SIBMessagingEngineQuery.class);
    
    private final String node;
    private final String server;
    private final String name;
    
    public SIBMessagingEngineQuery(String node, String server, String name) {
        this.node = node;
        this.server = server;
        this.name = name;
    }

    private static <T extends SIBDestination> T lookupDestination(Collection<T> destinations, SIBLocalizationPointCO localizationPoint) {
        for (T destination : destinations) {
            if (localizationPoint.getTargetUuid().equals(destination.getUuid())) {
                return destination;
            }
        }
        return null;
    }
    
    public SIBMessagingEngineInfo execute(Config config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        SIBMessagingEngineCO me = config.allScopes(node, server).path(SIBMessagingEngineCO.class, name).resolveAtMostOne(false);
        if (me == null) {
            return null;
        } else {
            List<SIBLocalizationPointInfo> localizationPoints = new ArrayList<SIBLocalizationPointInfo>();
            Collection<SIBQueue> queues = null;
            Collection<SIBTopicSpace> topicSpaces = null;
            for (SIBLocalizationPointCO localizationPoint : me.getLocalizationPoints()) {
                SIBDestinationType type;
                String destinationName;
                if (localizationPoint instanceof SIBQueueLocalizationPointCO) {
                    if (queues == null) {
                        queues = config.cell().path(SIBus.class, me.getBusName()).path(SIBQueue.class).resolve(false);
                    }
                    SIBQueue queue = lookupDestination(queues, localizationPoint);
                    if (queue == null) {
                        log.warn("No SIBQueue found for SIBQueueLocalizationPoint " + localizationPoint.getIdentifier());
                        continue;
                    }
                    List<SIBQualifiedDestinationName> path = queue.getDefaultForwardRoutingPath();
                    if (path != null && !path.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Ignoring queue " + queue.getIdentifier() + " because it has a default forward routing path: " + path);
                        }
                        continue;
                    }
                    type = SIBDestinationType.QUEUE;
                    destinationName = queue.getIdentifier();
                } else if (localizationPoint instanceof SIBTopicSpaceLocalizationPointCO) {
                    if (topicSpaces == null) {
                        topicSpaces = config.cell().path(SIBus.class, me.getBusName()).path(SIBTopicSpace.class).resolve(false);
                    }
                    SIBTopicSpace topicSpace = lookupDestination(topicSpaces, localizationPoint);
                    if (topicSpace == null) {
                        log.warn("No SIBTopicSpace found for SIBTopicSpaceLocalizationPoint " + localizationPoint.getIdentifier());
                        continue;
                    }
                    type = SIBDestinationType.TOPIC;
                    destinationName = topicSpace.getIdentifier();
                } else {
                    continue;
                }
                localizationPoints.add(new SIBLocalizationPointInfo(type, destinationName));
            }
            List<SIBGatewayLinkInfo> gatewayLinks = new ArrayList<SIBGatewayLinkInfo>();
            for (SIBGatewayLinkCO gatewayLink : me.getGatewayLink()) {
                gatewayLinks.add(new SIBGatewayLinkInfo(gatewayLink.getId(), gatewayLink.getName(), gatewayLink.getTargetUuid()));
            }
            return new SIBMessagingEngineInfo(me.getName(), me.getBusName(),
                    localizationPoints.toArray(new SIBLocalizationPointInfo[localizationPoints.size()]),
                    gatewayLinks.toArray(new SIBGatewayLinkInfo[gatewayLinks.size()]));
        }
    }

    @Override
    public int hashCode() {
        return 31*31*node.hashCode() + 31*server.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SIBMessagingEngineQuery) {
            SIBMessagingEngineQuery other = (SIBMessagingEngineQuery)obj;
            return other.node.equals(node) && other.server.equals(server) && other.name.equals(name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + "," + name + ")";
    }
}
