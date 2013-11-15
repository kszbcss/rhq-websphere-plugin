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
package com.ibm.websphere.management;

public class NotificationConstants {
    public static final String TYPE_J2EE_STATE = "j2ee.state";
    
    public static final String TYPE_J2EE_STATE_STARTING = "j2ee.state.starting";
    
    public static final String TYPE_J2EE_STATE_RUNNING = "j2ee.state.running";
    
    public static final String TYPE_J2EE_STATE_STOPPING = "j2ee.state.stopping";
    
    public static final String TYPE_J2EE_STATE_STOPPED = "j2ee.state.stopped";
    
    public static final String TYPE_J2EE_STATE_FAILED = "j2ee.state.failed";
    
    public static final String TYPE_PROCESS = "websphere.process";
    
    public static final String TYPE_PROCESS_STARTING = "websphere.process.starting";
    
    public static final String TYPE_PROCESS_RUNNING = "websphere.process.running";
    
    public static final String TYPE_PROCESS_STOPPING = "websphere.process.stopping";
    
    public static final String TYPE_PROCESS_STOPPED = "websphere.process.stopped";
    
    public static final String TYPE_PROCESS_FAILED = "websphere.process.failed";
    
    public static final String TYPE_PROCESS_NOT_MONITORED = "websphere.process.not.monitored";
    
    public static final String KEY_PROCESS_NAME = "processName";
    
    public static final String KEY_NODE_NAME = "nodeName";
    
    public static final String KEY_PROCESS_VERSION = "version";
    
    public static final String KEY_PROCESS_ID = "processId";
    
    public static final String TYPE_NODESYNC = "websphere.nodesync";
    
    public static final String TYPE_NODESYNC_INITIATED = "websphere.nodesync.initiated";
    
    public static final String TYPE_NODESYNC_COMPLETE = "websphere.nodesync.complete";
    
    public static final String TYPE_MULTISYNC_EVENT = "websphere.multisync.event";
    
    public static final String TYPE_DISCOVERY = "websphere.discovery";
    
    public static final String TYPE_DISCOVERY_PROCESS_FOUND = "websphere.discovery.process.found";
    
    public static final String TYPE_DISCOVERY_PROCESS_LOST = "websphere.discovery.process.lost";
    
    public static final String TYPE_DISCOVERY_AGENT_FOUND = "websphere.discovery.agent.found";
    
    public static final String TYPE_AGENT_DISCOVERED = "websphere.discovery.agent.found";
    
    public static final String TYPE_CLUSTER = "websphere.cluster";
    
    public static final String TYPE_CLUSTER_STARTING = "websphere.cluster.starting";
    
    public static final String TYPE_CLUSTER_PARTIAL_START = "websphere.cluster.partial.start";
    
    public static final String TYPE_CLUSTER_RUNNING = "websphere.cluster.running";
    
    public static final String TYPE_CLUSTER_STOPPING = "websphere.cluster.stopping";
    
    public static final String TYPE_CLUSTER_PARTIAL_STOP = "websphere.cluster.partial.stop";
    
    public static final String TYPE_CLUSTER_STOPPED = "websphere.cluster.stopped";
    
    public static final String TYPE_CLUSTER_WEIGHT_TABLE_CHANGE = "websphere.cluster.weight.table.change";
    
    public static final String TYPE_CLUSTER_WEIGHT_TABLE_ENTRY_CHANGE = "websphere.cluster.weight.table.entry.change";
    
    public static final String TYPE_ADDNODE = "websphere.addnode";
    
    public static final String TYPE_ADDNODE_STARTING = "websphere.addnode.starting";
    
    public static final String TYPE_ADDNODE_COMPLETE = "websphere.addnode.complete";
    
    public static final String TYPE_ADDNODE_FAILED = "websphere.addnode.failed";
    
    public static final String TYPE_ADDNODE_MESSAGE = "websphere.addnode.message";
    
    public static final String KEY_ADDNODE_NAME = "nodeName";
    
    public static final String TYPE_REMOVENODE = "websphere.removenode";
    
    public static final String KEY_REMOVENODE_NAME = "nodeName";
    
    public static final String TYPE_REMOVENODE_STARTING = "websphere.removenode.starting";
    
    public static final String TYPE_REMOVENODE_COMPLETE = "websphere.removenode.complete";
    
    public static final String TYPE_REMOVENODE_FAILED = "websphere.removenode.failed";
    
    public static final String TYPE_REMOVENODE_MESSAGE = "websphere.removenode.message";
    
    public static final String TYPE_REPOSITORY = "websphere.repository";
    
    public static final String TYPE_REPOSITORY_CHANGE_EVENT = "websphere.repository.change";
    
    public static final String TYPE_REPOSITORY_LOCK_EVENT = "websphere.repository.lock";
    
    public static final String TYPE_REPOSITORY_UNLOCK_EVENT = "websphere.repository.unlock";
    
    public static final String TYPE_REPOSITORY_EPOCH_REFRESH_EVENT = "websphere.repository.epoch.refresh";
    
    public static final String TYPE_APPMANAGEMENT = "websphere.admin.appmgmt";
    
    public static final String TYPE_APPMANAGEMENT_INSTALL = "websphere.admin.appmgmt.install";
    
    public static final String TYPE_APPMANAGEMENT_UNINSTALL = "websphere.admin.appmgmt.uninstall";
    
    public static final String TYPE_APPMANAGEMENT_UPDATE = "websphere.admin.appmgmt.update";
    
    public static final String TYPE_APPMANAGEMENT_SYNC = "websphere.admin.appmgmt.sync";
    
    public static final String TYPE_APPMANAGEMENT_UPDATECLUSTER = "websphere.admin.appmgmt.updatecluster";
    
    public static final String TYPE_WORKSPACE_MANAGER = "websphere.workspace.event";
    
    public static final String TYPE_RAS = "websphere.ras";
    
    public static final String TYPE_RAS_FATAL = "websphere.ras.fatal";
    
    public static final String TYPE_RAS_ERROR = "websphere.ras.error";
    
    public static final String TYPE_RAS_WARNING = "websphere.ras.warning";
    
    public static final String TYPE_RAS_AUDIT = "websphere.ras.audit";
    
    public static final String TYPE_RAS_SERVICE = "websphere.ras.service";
    
    public static final String TYPE_RAS_INFO = "websphere.ras.info";
    
    public static final String TYPE_THREAD_MONITOR_THREAD_HUNG = "websphere.thread.hung";
    
    public static final String TYPE_THREAD_MONITOR_THREAD_CLEAR = "websphere.thread.clear";
    
    public static final String KEY_THREAD_MONITOR_THREAD_NAME = "threadName";
    
    public static final String HANDLE_NOTIFICATION_TIMEOUT_KEY = "com.ibm.ws.management.event.LocalNotificationService.handleNotificationTimeout";
    
    public static final long HANDLE_NOTIFICATION_TIMEOUT_DEFAULT = 300000L;
    
    public static final String LOCAL_NOTIFICATION_SERVICE_THREAD_KEEPALIVE_TIME = "com.ibm.ws.management.event.LocalNotificationService.threadKeepAliveTime";
    
    public static final long LOCAL_NOTIFICATION_SERVICE_THREAD_KEEPALIVE_TIME_DEFAULT = 60000L;
    
    public static final String HEAPDUMP_ATTEMPTING = "websphere.jvm.heapdump.attempting";
    
    public static final String HEAPDUMP_GENERATED = "websphere.jvm.heapdump.generated";
    
    public static final String ADVISOR_HEAPDUMP_AUTOMATED_PROCESS_STARTED = "websphere.advisor.heapdump.automatedHeapDumpProcessStarted";
    
    public static final String ADVISOR_HEAPDUMP_AUTOMOATED_PROCESS_FINISHED = "websphere.advisor.heapdump.AutomatedHeapDumpProcessFinished";
    
    public static final String SYSTEMDUMP_ATTEMPTING = "websphere.jvm.systemdump.attempting";
    
    public static final String SYSTEMDUMP_GENERATED = "websphere.jvm.systempdump.generated";
    
    public static final String ADMIN_AGENT_SUBSYSTEM_INIT = "websphere.management.admin.agent.subsystem.init";
    
    public static final String ADMIN_AGENT_SUBSYSTEM_DESTROY = "websphere.management.admin.agent.subsystem.destroy";
    
    public static final String ADMIN_AGENT_SUBSYSTEM_START = "websphere.management.admin.agent.subsystem.start";
    
    public static final String ADMIN_AGENT_SUBSYSTEM_STOP = "websphere.management.admin.agent.subsystem.stop";
}
