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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryException;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBGatewayLinkCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBLocalizationPointCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBMessagingEngineCO;
import be.fgov.kszbcss.rhq.websphere.config.types.SIBQueueLocalizationPointCO;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBMessagingEngineQuery implements ConfigQuery<SIBMessagingEngineInfo[]> {
    private static final long serialVersionUID = 4836748290502191854L;
    
    private final String node;
    private final String server;
    
    public SIBMessagingEngineQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public SIBMessagingEngineInfo[] execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        List<SIBMessagingEngineInfo> result = new ArrayList<SIBMessagingEngineInfo>();
        for (SIBMessagingEngineCO me : config.allScopes(node, server).path(SIBMessagingEngineCO.class).resolve()) {
            List<SIBLocalizationPointInfo> localizationPoints = new ArrayList<SIBLocalizationPointInfo>();
            for (SIBLocalizationPointCO localizationPoint : me.getLocalizationPoints()) {
                String identifier = localizationPoint.getIdentifier();
                localizationPoints.add(new SIBLocalizationPointInfo(
                        localizationPoint instanceof SIBQueueLocalizationPointCO ? SIBLocalizationPointType.QUEUE : SIBLocalizationPointType.TOPIC,
                        identifier.substring(0, identifier.indexOf('@'))));
            }
            List<SIBGatewayLinkInfo> gatewayLinks = new ArrayList<SIBGatewayLinkInfo>();
            for (SIBGatewayLinkCO gatewayLink : me.getGatewayLink()) {
                gatewayLinks.add(new SIBGatewayLinkInfo(gatewayLink.getId(), gatewayLink.getName(), gatewayLink.getTargetUuid()));
            }
            result.add(new SIBMessagingEngineInfo(me.getName(), me.getBusName(),
                    localizationPoints.toArray(new SIBLocalizationPointInfo[localizationPoints.size()]),
                    gatewayLinks.toArray(new SIBGatewayLinkInfo[gatewayLinks.size()])));
        }
        return result.toArray(new SIBMessagingEngineInfo[result.size()]);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SIBMessagingEngineQuery) {
            SIBMessagingEngineQuery other = (SIBMessagingEngineQuery)obj;
            return other.node.equals(node) && other.server.equals(server);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + ")";
    }
}
