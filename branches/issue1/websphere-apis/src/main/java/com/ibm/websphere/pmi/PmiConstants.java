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
package com.ibm.websphere.pmi;

public interface PmiConstants {
    public static final java.lang.String PLATFORM_ALL = "all";
    
    public static final java.lang.String PLATFORM_DISTRIBUTED = "distributed";
    
    public static final java.lang.String PLATFORM_ZOS = "zos";
    
    public static final int UNKNOWN_ID = -1;
    
    @Deprecated
    public static final int NOT_IN_SUBMODULE = -1;
    
    public static final int ALL_DATA = -3;
    
    public static final int JAVA_TIME_CONVERT_RATIO = 1;
    
    public static final int LEVEL_DISABLE = 1000;
    
    @Deprecated
    public static final int LEVEL_ENABLE = 2;
    
    @Deprecated
    public static final int LEVEL_MAX = 15;
    
    @Deprecated
    public static final int LEVEL_HIGH = 7;
    
    @Deprecated
    public static final int LEVEL_MEDIUM = 3;
    
    @Deprecated
    public static final int LEVEL_LOW = 1;
    
    @Deprecated
    public static final int LEVEL_NONE = 0;
    
    @Deprecated
    public static final int LEVEL_UNDEFINED = -1;
    
    public static final int LEVEL_FINEGRAIN = -2;
    
    @Deprecated
    public static final int UNINITIALIZED = 0;
    
    @Deprecated
    public static final int INITIALIZING = 1;
    
    @Deprecated
    public static final int INITIALIZATION_FAILED = 2;
    
    @Deprecated
    public static final int RUNNING = 3;
    
    @Deprecated
    public static final int TERMINATING = 4;
    
    @Deprecated
    public static final int STOPPED = 5;
    
    @Deprecated
    public static final int LOST_CONTACT = 6;
    
    @Deprecated
    public static final int AE_40 = 1;
    
    @Deprecated
    public static final int AES_40 = 2;
    
    @Deprecated
    public static final int AE_35 = 3;
    
    @Deprecated
    public static final java.lang.String LEVEL_NONE_STRING = "none";
    
    @Deprecated
    public static final java.lang.String LEVEL_LOW_STRING = "low";
    
    @Deprecated
    public static final java.lang.String LEVEL_MEDIUM_STRING = "medium";
    
    @Deprecated
    public static final java.lang.String LEVEL_HIGH_STRING = "high";
    
    @Deprecated
    public static final java.lang.String LEVEL_MAX_STRING = "maximum";
    
    public static final int TYPE_UNDEFINED = -1;
    
    @Deprecated
    public static final int TYPE_INT = 1;
    
    public static final int TYPE_LONG = 2;
    
    public static final int TYPE_DOUBLE = 3;
    
    public static final int TYPE_STAT = 4;
    
    public static final int TYPE_LOAD = 5;
    
    public static final int TYPE_AVGSTAT = 6;
    
    public static final int TYPE_RANGE = 7;
    
    public static final int TYPE_INVALID = -1;
    
    public static final int TYPE_ROOT = 10;
    
    public static final int TYPE_NODE = 11;
    
    public static final int TYPE_SERVER = 12;
    
    public static final int TYPE_MODULE = 13;
    
    public static final int TYPE_INSTANCE = 14;
    
    public static final int TYPE_SUBMODULE = 15;
    
    public static final int TYPE_SUBINSTANCE = 16;
    
    public static final int TYPE_COLLECTION = 17;
    
    public static final int TYPE_DATA = 18;
    
    public static final int TYPE_CATEGORY = 19;
    
    public static final int TYPE_MODULEROOT = 24;
    
    @Deprecated
    public static final java.lang.String XML_START = "<";
    
    @Deprecated
    public static final java.lang.String XML_ENDLINE = "\">\n";
    
    @Deprecated
    public static final java.lang.String XML_ENDTAG = "\"/>\n";
    
    @Deprecated
    public static final java.lang.String XML_ENDCOLLECTION = "</PerfCollection>\n";
    
    @Deprecated
    public static final java.lang.String XML_ENDMODULE = "</PerfModule>\n";
    
    @Deprecated
    public static final java.lang.String XML_ENDSERVER = "</PerfServer>\n";
    
    @Deprecated
    public static final java.lang.String XML_ENDNODE = "</PerfNode>\n";
    
    @Deprecated
    public static final java.lang.String XML_INT = "<PerfInt";
    
    @Deprecated
    public static final java.lang.String XML_LONG = "<PerfLong";
    
    @Deprecated
    public static final java.lang.String XML_DOUBLE = "<PerfDouble";
    
    @Deprecated
    public static final java.lang.String XML_STAT = "<PerfStat";
    
    @Deprecated
    public static final java.lang.String XML_LOAD = "<PerfLoad";
    
    @Deprecated
    public static final java.lang.String XML_COLLECTION = "PerfCollection";
    
    @Deprecated
    public static final java.lang.String XML_MODULE = "<PerfModule";
    
    @Deprecated
    public static final java.lang.String XML_SERVER = "<PerfServer";
    
    @Deprecated
    public static final java.lang.String XML_NODE = "<PerfNode";
    
    @Deprecated
    public static final java.lang.String XML_VIEW = "<PerfView";
    
    @Deprecated
    public static final java.lang.String XML_QUOTE = "\"";
    
        @Deprecated
    public static final java.lang.String XML_ID = " ID=\"";
    
        @Deprecated
    public static final java.lang.String XML_NAME = " name=\"";
    
        @Deprecated
    public static final java.lang.String XML_TIME = "\" time=\"";
    
    @Deprecated
    public static final java.lang.String XML_VALUE = "\" value=\"";
    
    @Deprecated
    public static final java.lang.String XML_COUNT = "\" count=\"";
    
    @Deprecated
    public static final java.lang.String XML_TOTAL = "\" total=\"";
    
    @Deprecated
    public static final java.lang.String XML_SUMOFSQUARES = "\" sumOfSquares=\"";
    
    @Deprecated
    public static final java.lang.String XML_LASTVALUE = "\" lastValue=\"";
    
    @Deprecated
    public static final java.lang.String XML_INTEGRAL = "\" integral=\"";
    
    @Deprecated
    public static final java.lang.String XML_CREATETIME = "\" createTime=\"";
    
    @Deprecated
    public static final java.lang.String DEFAULT_MODULE_PREFIX = "com.ibm.websphere.pmi.xml.";
    
    public static final java.lang.String BEAN_MODULE = "beanModule";
    
    public static final java.lang.String BEAN_METHODS_SUBMODULE = "beanModule.methods";
    
    public static final java.lang.String CONNPOOL_MODULE = "connectionPoolModule";
    
    public static final java.lang.String SYSTEM_MODULE = "systemModule";
    
    public static final java.lang.String J2C_MODULE = "j2cModule";
    
    public static final java.lang.String J2C_DS = "DataSource";
    
    public static final java.lang.String J2C_CF = "ConnectionFactory";
    
    public static final java.lang.String J2C_JMS_CONNECTIONS = "jmsConnections";
    
    public static final java.lang.String THREADPOOL_MODULE = "threadPoolModule";
    
    public static final java.lang.String TRAN_MODULE = "transactionModule";
    
    public static final java.lang.String RUNTIME_MODULE = "jvmRuntimeModule";
    
    public static final java.lang.String JVMPI_MODULE = "jvmpiModule";
    
    public static final java.lang.String ORBPERF_MODULE = "orbPerfModule";
    
    public static final java.lang.String INTERCEPTOR_SUBMODULE = "orbPerfModule.interceptors";
    
    public static final java.lang.String WEBAPP_MODULE = "webAppModule";
    
    public static final java.lang.String SERVLET_SUBMODULE = "webAppModule.servlets";
    
    public static final java.lang.String SESSIONS_MODULE = "servletSessionsModule";
    
    public static final java.lang.String CACHE_MODULE = "cacheModule";
    
    public static final java.lang.String TEMPLATE_SUBMODULE = "cacheModule.template";
    
    public static final java.lang.String APPSERVER_MODULE = "pmi";
    
    public static final java.lang.String WSGW_MODULE = "wsgwModule";
    
    public static final java.lang.String WLM_MODULE = "wlmModule";
    
    public static final java.lang.String WLM_SERVER_MODULE = "wlmModule.server";
    
    public static final java.lang.String WLM_CLIENT_MODULE = "wlmModule.client";
    
    public static final java.lang.String WEBSERVICES_MODULE = "webServicesModule";
    
    public static final java.lang.String WEBSERVICES_SUBMODULE = "webServicesModule.services";
    
    public static final java.lang.String METHODS_SUBMODULE_SHORTNAME = "methods";
    
    public static final java.lang.String SERVLETS_SUBMODULE_SHORTNAME = "servlets";
    
    public static final java.lang.String ROOT_NAME = "pmiroot";
    
    @Deprecated
    public static final java.lang.String ROOT_DESC = "pmiroot.desc";
    
    @Deprecated
    public static final java.lang.String COLLECTION_DESC = ".col";
    
    @Deprecated
    public static final java.lang.String PMI_DISABLE_STRING = "pmi=disable";
    
    @Deprecated
    public static final java.lang.String LOAD_AVG = "pmi.avg";
    
    @Deprecated
    public static final java.lang.String MSG_BUNDLE = "com.ibm.ws.pmi.properties.PMIMessages";
    
    public static final java.lang.String EJB_ENTITY = "ejb.entity";
    
    public static final java.lang.String EJB_STATEFUL = "ejb.stateful";
    
    public static final java.lang.String EJB_STATELESS = "ejb.stateless";
    
    public static final java.lang.String EJB_MESSAGEDRIVEN = "ejb.messageDriven";
}
