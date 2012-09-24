package be.fgov.kszbcss.rhq.websphere.config;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigQueryServiceHandle implements ConfigQueryService {
    private static final Log log = LogFactory.getLog(ConfigQueryServiceHandle.class);
    
    private DeploymentManagerConnection dmc;
    
    ConfigQueryServiceHandle(DeploymentManagerConnection dmc) {
        this.dmc = dmc;
    }
    
    public <T extends Serializable> T query(ConfigQuery<T> query, boolean immediate) throws InterruptedException {
        return dmc.getConfigQueryService().query(query, immediate);
    }
    
    public void release() {
        if (log.isDebugEnabled()) {
            log.debug("Releasing one instance of ConfigQueryServiceHandle for cell " + dmc.getConfigQueryService().getCell());
        }
        dmc.decrementRefCount();
        dmc = null;
    }
}
