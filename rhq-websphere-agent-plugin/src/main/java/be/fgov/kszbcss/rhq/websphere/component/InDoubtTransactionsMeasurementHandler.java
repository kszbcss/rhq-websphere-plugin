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
package be.fgov.kszbcss.rhq.websphere.component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.SimpleMeasurementHandler;

/**
 * {@link MeasurementHandler} implementation that counts in-doubt transactions. This class solves
 * the problem that a transaction can only be considered as in-doubt if it has been reported twice
 * by the server.
 */
public abstract class InDoubtTransactionsMeasurementHandler extends SimpleMeasurementHandler {
    private static final Logger log = LoggerFactory.getLogger(InDoubtTransactionsMeasurementHandler.class);
    
    private Set<String> transactionIds;
    
    @Override
    protected final Object getValue() throws JMException, ConnectorException {
        if (transactionIds == null) {
            // This is the first measurement; just initialize the transaction IDs and return nothing
            transactionIds = new HashSet<String>(getTransactionIds());
            return null;
        } else {
            int confirmedCount = 0;
            Set<String> newTransactionIds = getTransactionIds();
            for (Iterator<String> it = transactionIds.iterator(); it.hasNext(); ) {
                String id = it.next();
                if (!newTransactionIds.contains(id)) {
                    it.remove();
                    if (log.isDebugEnabled()) {
                        log.debug("Transaction " + id + " is no longer in-doubt");
                    }
                }
            }
            for (String id : newTransactionIds) {
                if (transactionIds.add(id)) {
                    // New transaction ID
                    if (log.isDebugEnabled()) {
                        log.debug("New (unconfirmed) in-doubt transaction " + id);
                    }
                } else {
                    // The transaction ID was already contained in the set
                    confirmedCount++;
                }
            }
            return confirmedCount;
        }
    }
    
    protected abstract Set<String> getTransactionIds() throws JMException, ConnectorException;
}
