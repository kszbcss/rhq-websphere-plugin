package be.fgov.kszbcss.rhq.websphere.config;

import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.repository.ConfigEpoch;

class ConfigQueryResultFactory implements UpdatingCacheEntryFactory {
    private static final Log log = LogFactory.getLog(ConfigQueryResultFactory.class);
    
    private final ConfigQueryServiceFactory factory;
    
    public ConfigQueryResultFactory(ConfigQueryServiceFactory factory) {
        this.factory = factory;
    }

    public Object createEntry(Object _key) throws Exception {
        ConfigQueryKey key = (ConfigQueryKey)_key;
        DeploymentManagerConnection dmc = factory.lookupDeploymentManagerConnection(key.getCell());
        ConfigEpoch epoch = dmc.getEpoch();
        if (epoch == null) {
            throw new RuntimeException("Deployment manager is unavailable");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + key);
            }
            ConfigQueryResult result = new ConfigQueryResult();
            result.epoch = epoch;
            result.object = key.getQuery().execute(dmc.getCellConfiguration());
            return result;
        }
    }

    public void updateEntryValue(Object _key, Object value) throws Exception {
        ConfigQueryKey key = (ConfigQueryKey)_key;
        DeploymentManagerConnection dmc = factory.lookupDeploymentManagerConnection(key.getCell());
        ConfigEpoch epoch = dmc.getEpoch();
        if (epoch == null) {
            if (log.isDebugEnabled()) {
                log.debug("Deployment manager is unavailable; returning potentially stale object for query: " + key);
            }
        } else {
            ConfigQueryResult result = (ConfigQueryResult)value;
            if (result.epoch.equals(epoch)) {
                if (log.isDebugEnabled()) {
                    log.debug("Not updating result for the following query: " + key);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Reexecuting query: " + key);
                }
                Object resultObject;
                try {
                    resultObject = key.getQuery().execute(dmc.getCellConfiguration());
                } catch (InterruptedException ex) {
                    // Ehcache will swallow the exception in UpdatingSelfPopulatingCache#update, which is not
                    // a good thing. Therefore we set the interrupt flag (which will be checked in
                    // ConfigQueryService#query) and rethrow the exception.
                    Thread.currentThread().interrupt();
                    throw ex;
                }
                // Only update fields if no exception is thrown:
                result.epoch = epoch;
                result.object = resultObject;
            }
        }
    }
}
