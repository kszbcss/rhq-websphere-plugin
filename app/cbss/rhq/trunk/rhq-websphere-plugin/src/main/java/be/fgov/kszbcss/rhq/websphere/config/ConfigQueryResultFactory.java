package be.fgov.kszbcss.rhq.websphere.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.config.cache.CacheRefreshException;
import be.fgov.kszbcss.rhq.websphere.config.cache.DelayedRefreshCacheEntryFactory;

import com.ibm.websphere.management.repository.ConfigEpoch;

class ConfigQueryResultFactory implements DelayedRefreshCacheEntryFactory<ConfigQuery<?>,ConfigQueryResult> {
    private static final Log log = LogFactory.getLog(ConfigQueryResultFactory.class);
    
    private final DeploymentManagerConnection dmc;
    
    public ConfigQueryResultFactory(DeploymentManagerConnection dmc) {
        this.dmc = dmc;
    }

    public ConfigQueryResult createEntry(ConfigQuery<?> key) throws CacheRefreshException {
        ConfigEpoch epoch = dmc.getEpoch();
        if (epoch == null) {
            throw new CacheRefreshException("Deployment manager is unavailable");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + key);
            }
            ConfigQueryResult result = new ConfigQueryResult();
            result.epoch = epoch;
            try {
                result.object = key.execute(dmc.getCellConfiguration());
            } catch (Exception ex) {
                // TODO: review this
                throw new CacheRefreshException(ex);
            }
            return result;
        }
    }

    public boolean isStale(ConfigQuery<?> key, ConfigQueryResult value) {
        ConfigEpoch epoch = dmc.getEpoch();
        if (epoch == null) {
            if (log.isDebugEnabled()) {
                log.debug("Deployment manager is unavailable; returning potentially stale object for query: " + key);
            }
            return false;
        } else {
            ConfigQueryResult result = (ConfigQueryResult)value;
            if (result.epoch.equals(epoch)) {
                if (log.isDebugEnabled()) {
                    log.debug("Not updating result for the following query: " + key);
                }
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Reexecuting query: " + key);
                }
                return true;
            }
        }
    }
}
