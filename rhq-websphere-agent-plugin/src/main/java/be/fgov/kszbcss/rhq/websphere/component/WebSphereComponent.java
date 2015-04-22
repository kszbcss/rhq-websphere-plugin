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
package be.fgov.kszbcss.rhq.websphere.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jmx.JMXComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.kszbcss.rhq.websphere.config.ConfigData;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.process.ApplicationServer;

public abstract class WebSphereComponent<T extends ResourceComponent<?>> implements JMXComponent<T> {
    private static final Logger log = LoggerFactory.getLogger(WebSphereComponent.class);
    
    private ResourceContext<T> resourceContext;
    private final List<ConfigQuery<?>> registeredConfigQueries = new ArrayList<ConfigQuery<?>>();

    @Override
	public final void start(ResourceContext<T> context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
        doStart();
    }
    
    public final ResourceContext<T> getResourceContext() {
        return resourceContext;
    }

    /**
     * Initialize the resource component.
     * <p>
     * <b>Note:</b> In contrast to {@link ResourceComponent#start(ResourceContext)} this method is
     * not allowed to throw checked exceptions. The rationale for this is to enforce the following
     * design principle in the WebSphere plug-in: a resource component must not connect to WebSphere
     * during the component startup; instead all connections to WebSphere must be established lazily
     * (e.g. when checking the availability or retrieving metrics). This ensures that the plug-in
     * works correctly even if some WebSphere instances are not running when the agent starts. The
     * plug-in provides the necessary utility classes (such as {@link MBeanClient}) to enable lazy
     * connections.
     * 
     * @throws InvalidPluginConfigurationException
     *             if the resource component could not be started because of a bad plug-in
     *             configuration
     * 
     * @see ResourceComponent#start(ResourceContext)
     */
    protected abstract void doStart() throws InvalidPluginConfigurationException;
    
    @Override
	public final void stop() {
        doStop();
        for (ConfigQuery<?> query : registeredConfigQueries) {
            getServer().unregisterConfigQuery(query);
        }
        registeredConfigQueries.clear();
        // The destroy hook is mainly used by WebSphereServerComponent: we need to destroy
        // the server object after unregistering the config queries
        destroy();
    }
    
    protected void doStop() {}
    protected void destroy() {}

    public abstract String getNodeName();
    public abstract String getServerName();
    
    public abstract ApplicationServer getServer();
    
    protected final <S extends Serializable> ConfigData<S> registerConfigQuery(ConfigQuery<S> query) {
        registeredConfigQueries.add(query);
        return getServer().registerConfigQuery(query);
    }
    
    /**
     * Determine whether the resource corresponding to this component is still present in the
     * WebSphere configuration.
     * 
     * @return <code>true</code> if the resource is present in the WebSphere configuration,
     *         <code>false</code> otherwise
     * @throws Exception 
     */
    protected abstract boolean isConfigured() throws Exception;

    @Override
	public final AvailabilityType getAvailability() {
        try {
            if (!isConfigured()) {
				log.debug("isConfigured=false => availability == MISSING");
				return AvailabilityType.MISSING;
            } else {
                log.debug("isConfigured=true; continue with checking the runtime state");
            }
        } catch (Exception ex) {
			log.warn("Caught exception thrown by isConfigured => availability == UNKNOWN", ex);
			return AvailabilityType.UNKNOWN;
        }
        return doGetAvailability();
    }
    
    protected abstract AvailabilityType doGetAvailability();

}
