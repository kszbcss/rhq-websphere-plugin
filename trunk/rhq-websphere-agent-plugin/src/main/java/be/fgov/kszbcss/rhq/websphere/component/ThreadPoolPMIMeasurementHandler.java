/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012,2014 Crossroads Bank for Social Security
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

import com.ibm.websphere.pmi.stat.WSRangeStatistic;

import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public class ThreadPoolPMIMeasurementHandler extends PMIMeasurementHandler {
    public ThreadPoolPMIMeasurementHandler(String... path) {
        super(path);
    }

    public ThreadPoolPMIMeasurementHandler(PMIModuleSelector moduleSelector) {
        super(moduleSelector);
    }

    @Override
    protected double getValue(String name, WSRangeStatistic statistic) {
        if (name.equals("PercentMaxed")) {
            return ((double)statistic.getCurrent())/100;
        } else {
            return super.getValue(name, statistic);
        }
    }
}
