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
package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SIBMessagingEngineInfo implements Serializable {
    private static final long serialVersionUID = 4353852405954930115L;

    private final String name;
    private final String busName;
    private final SIBLocalizationPointInfo[] localizationPoints;
    private final SIBGatewayLinkInfo[] gatewayLinks;
    
    public SIBMessagingEngineInfo(String name, String busName, SIBLocalizationPointInfo[] localizationPoints, SIBGatewayLinkInfo[] gatewayLinks) {
        this.name = name;
        this.busName = busName;
        this.localizationPoints = localizationPoints;
        this.gatewayLinks = gatewayLinks;
    }

    public String getName() {
        return name;
    }

    public String getBusName() {
        return busName;
    }
    
    public String[] getDestinationNames(SIBLocalizationPointType type) {
        List<String> result = new ArrayList<String>();
        for (SIBLocalizationPointInfo localizationPoint : localizationPoints) {
            if (localizationPoint.getType().equals(type)) {
                result.add(localizationPoint.getDestinationName());
            }
        }
        return result.toArray(new String[result.size()]);
    }
    
    public String[] getGatewayLinkNames() {
        String[] result = new String[gatewayLinks.length];
        for (int i = 0; i<gatewayLinks.length; i++) {
            result[i] = gatewayLinks[i].getName();
        }
        return result;
    }
    
    /**
     * Get the targetUuid for the gateway link with the given name.
     * 
     * @param name
     *            the name of the gateway link
     * @return the targetUuid for the gateway link, or <code>null</code> if no gateway link with the
     *         given name was found
     */
    public String getTargetUUIDForGatewayLink(String name) {
        for (SIBGatewayLinkInfo gatewayLink : gatewayLinks) {
            if (gatewayLink.getName().equals(name)) {
                return gatewayLink.getTargetUuid();
            }
        }
        return null;
    }

    public String getGatewayLinkId(String name) {
        for (SIBGatewayLinkInfo gatewayLink : gatewayLinks) {
            if (gatewayLink.getName().equals(name)) {
                return gatewayLink.getId();
            }
        }
        return null;
    }
}
