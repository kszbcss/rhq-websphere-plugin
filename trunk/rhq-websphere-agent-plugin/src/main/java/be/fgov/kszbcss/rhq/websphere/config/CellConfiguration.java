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
package be.fgov.kszbcss.rhq.websphere.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClientProxy;
import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigRepository;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.application.client.AppDeploymentTask;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Facade that gives access to various parts of the cell configuration. This includes:
 * <ul>
 * <li>Access to the configuration service to query configuration objects.
 * <li>Access to the configuration repository to query configuration documents.
 * <li>Access to the <tt>AppManagement</tt> MBean to retrieve information about a deployed
 * application.
 * </ul>
 */
public class CellConfiguration {
    private static final Log log = LogFactory.getLog(CellConfiguration.class);
    
    private final String cell;
    private final ConfigService configService;
    private final ConfigRepository configRepository;
    private final AppManagement appManagement;
    private final Path root;
    private final ReadWriteLock sessionLock = new ReentrantReadWriteLock();
    private boolean destroyed;
    private Session session;
    
    CellConfiguration(String cell, ConfigService configService, ConfigRepository configRepository, AppManagement appManagement) {
        this.cell = cell;
        this.configService = configService;
        this.configRepository = configRepository;
        this.appManagement = appManagement;
        root = new RootPath(this);
    }
    
    public String getCell() {
        return cell;
    }

    /**
     * Get the WebSphere version. The returned value is actually the version number of the deployment
     * manager (which may differ from the version number of the application servers).
     * 
     * @return
     * @throws JMException
     * @throws ConnectorException
     * @throws InterruptedException 
     */
    public String getWebSphereVersion() throws JMException, ConnectorException, InterruptedException {
        return ((MBeanClientProxy)configService).getMBeanClient().getObjectName(false).getKeyProperty("version");
    }
    
    <T> T execute(SessionAction<T> action) throws JMException, ConnectorException, InterruptedException {
        // Note: a read lock can't be upgraded to a write lock, so we need to acquire a write
        // lock first.
        Lock readLock = sessionLock.readLock();
        Lock writeLock = sessionLock.writeLock();
        writeLock.lockInterruptibly();
        try {
            if (destroyed) {
                throw new IllegalStateException("Object already destroyed; not accepting any new requests");
            }
            if (session == null) {
                session = new Session("rhq-websphere-plugin", false);
                if (log.isDebugEnabled()) {
                    log.debug("New session created: " + session);
                }
            }
            readLock.lockInterruptibly();
        } finally {
            writeLock.unlock();
        }
        try {
            return action.execute(configService, appManagement, session);
        } finally {
            readLock.unlock();
        }
    }
    
    public Path path(String type, String name) {
        return root.path(type, name);
    }
    
    public Path path(String type) {
        return root.path(type);
    }
    
    public Path cell() {
        return path("Cell", cell);
    }
    
    public Path node(String nodeName) {
        return cell().path("Node", nodeName);
    }
    
    public Path server(String nodeName, String serverName) {
        return node(nodeName).path("Server", serverName);
    }
    
    public Path allScopes(String nodeName, String serverName) throws JMException, ConnectorException, InterruptedException, ConfigQueryException {
        Path cell = cell();
        Path node = cell.path("Node", nodeName);
        Path server = node.path("Server", serverName);
        ConfigObject serverObject = server.resolveSingle();
        Path cluster = cell.path("ServerCluster", (String)serverObject.getAttribute("clusterName"));
        // Order is important here: we return objects with higher precedence first
        return new PathGroup(server, cluster, node, cell);
    }
    
    ConfigObject[] resolve(final String containmentPath) throws JMException, ConnectorException, InterruptedException {
        ObjectName[] objectNames = execute(new SessionAction<ObjectName[]>() {
            public ObjectName[] execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException {
                return configService.resolve(session, containmentPath);
            }
        });
        ConfigObject[] result = new ConfigObject[objectNames.length];
        for (int i=0; i<objectNames.length; i++) {
            result[i] = new ConfigObject(this, objectNames[i]);
        }
        return result;
    }
    
    public String[] listResourceNames(String parent, int type, int depth) throws JMException, ConnectorException {
        return configRepository.listResourceNames(parent, type, depth);
    }

    public byte[] extract(String docURI) throws JMException, ConnectorException {
        try {
            InputStream in = configRepository.extract(docURI).getSource();
            try {
                return IOUtils.toByteArray(in);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new ConnectorException(ex);
        }
    }
    
    public Map<String,List<Map<String,String>>> getApplicationInfo(final String appName) throws JMException, ConnectorException, InterruptedException {
        Map<String,List<Map<String,String>>> result = execute(new SessionAction<Map<String,List<Map<String,String>>>>() {
            public Map<String,List<Map<String,String>>> execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException {
                // workspaceId = session.toString() as explained in the Javadoc of SessionAction
                Vector<AppDeploymentTask> tasks = appManagement.getApplicationInfo(appName, new Hashtable(), session.toString());
                Map<String,List<Map<String,String>>> result = new HashMap<String,List<Map<String,String>>>();
                for (AppDeploymentTask task : tasks) {
                    // The task data is organized as a table where the first row is a header
                    String[][] data = task.getTaskData();
                    if (data != null) {
                        List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
                        for (int rowIndex=1; rowIndex<data.length; rowIndex++) {
                            Map<String,String> row = new HashMap<String,String>();
                            for (int colIndex=0; colIndex<data[0].length; colIndex++) {
                                row.put(data[0][colIndex], data[rowIndex][colIndex]);
                            }
                            rows.add(row);
                        }
                        result.put(task.getName(), rows);
                    }
                }
                return result;
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("Loaded application info for " + appName + ": " + result);
        }
        return result;
    }
    
    private void discardSession(boolean destroy) {
        Lock writeLock = sessionLock.writeLock();
        writeLock.lock();
        try {
            if (session != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Discarding session " + session);
                }
                try {
                    configService.discard(session);
                } catch (Exception ex) {
                    log.warn("Unexpected exception when discarding workspace " + session, ex);
                }
            }
            if (destroy) {
                destroyed = true;
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    void refresh() {
        // There seems to be no JMX operation that allows to refresh the workspace (I'm wondering
        // how the admin console actually does this...), so we simply discard the session. Next time
        // a ConfigService method is called, a new session will be created automatically.
        discardSession(false);
    }
    
    void destroy() {
        discardSession(true);
    }
}
