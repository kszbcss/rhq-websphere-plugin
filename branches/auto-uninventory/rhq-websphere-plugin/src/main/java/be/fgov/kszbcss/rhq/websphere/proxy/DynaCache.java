package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface DynaCache {
    String[] getCacheInstanceNames() throws JMException, ConnectorException;
    String[] getAllCacheStatistics(String cacheInstance) throws JMException, ConnectorException;
    void clearCache(String cacheInstance) throws JMException, ConnectorException;
}
