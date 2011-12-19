package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class WorkManagerThreadPoolMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final String jndiName;

    public WorkManagerThreadPoolMBeanLocator(String jndiName) {
        super("WebSphere", false);
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException {
        ManagedServer server = (ManagedServer)processInfo.getServer();
        String name = server.queryConfig(new WorkManagerMapQuery(server.getNode(), server.getServer())).get(jndiName);
        if (name == null) {
            throw new JMException("No work manager found for JNDI name " + jndiName);
        }
        props.put("type", "ThreadPool");
        props.put("name", "WorkManager." + name);
    }
    
    // TODO: implement equals and hashCode

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + jndiName + ")";
    }
}
