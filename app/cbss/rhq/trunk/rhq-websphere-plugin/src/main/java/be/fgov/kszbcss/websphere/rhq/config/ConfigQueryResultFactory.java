package be.fgov.kszbcss.websphere.rhq.config;

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
            ConfigQueryResult result = new ConfigQueryResult();
            result.epoch = epoch;
            result.object = key.getQuery().execute(dmc.getConfigService());
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
                Object resultObject = key.getQuery().execute(dmc.getConfigService());
                // Only update fields if no exception is thrown:
                result.epoch = epoch;
                result.object = resultObject;
            }
        }
    }
}
