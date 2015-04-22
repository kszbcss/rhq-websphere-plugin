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
package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rhq.core.domain.measurement.AvailabilityType;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClientProxy;
import be.fgov.kszbcss.rhq.websphere.proxy.EJBMonitor;

import com.ibm.websphere.pmi.PmiConstants;

public class StatelessSessionBeanComponent extends EnterpriseBeanComponent {
    private static final Logger log = LoggerFactory.getLogger(StatelessSessionBeanComponent.class);
    
    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.STATELESS_SESSION;
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.EJB_STATELESS;
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        EJBMonitor ejbMonitor = getApplication().getParent().getEjbMonitor();
        try {
            if (((MBeanClientProxy)ejbMonitor).getMBeanClient().isRegistered()) {
                // TODO: once XM4WAS 0.4.0 has been released, use the variant of the validateStatelessSessionBean method that takes a timeout (and set it to 3 seconds)
                String result = ejbMonitor.validateStatelessSessionBean(getApplicationName(), getModuleName(), getBeanName());
                if (result != null && log.isDebugEnabled()) {
                    log.debug("validateStatelessSessionBean result:\n" + result);
                }
                return result == null ? AvailabilityType.UP : AvailabilityType.DOWN;
            } else {
                log.debug("EJBMonitor not available => availability = UP");
                return AvailabilityType.UP;
            }
        } catch (Exception ex) {
            log.debug("Caught exception => availability = DOWN", ex);
            return AvailabilityType.DOWN;
        }
    }
}
