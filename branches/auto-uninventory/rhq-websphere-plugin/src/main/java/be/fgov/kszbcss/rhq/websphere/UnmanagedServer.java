package be.fgov.kszbcss.rhq.websphere;

import javax.management.JMException;

import org.rhq.core.domain.configuration.Configuration;

import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryService;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQueryServiceFactory;

import com.ibm.websphere.management.exception.ConnectorException;

public class UnmanagedServer extends ApplicationServer {
    public UnmanagedServer(String cell, String node, String server, Configuration config) {
        super(cell, node, server, "UnManagedProcess", new ConfigurationBasedProcessLocator(config));
    }

    @Override
    protected ConfigQueryService createConfigQueryService() throws ConnectorException {
        return ConfigQueryServiceFactory.getInstance().getConfigQueryService(this);
    }

    @Override
    public String getClusterName() throws InterruptedException, JMException, ConnectorException {
        // An unmanaged server cannot be a member of a cluster
        return null;
    }
}
