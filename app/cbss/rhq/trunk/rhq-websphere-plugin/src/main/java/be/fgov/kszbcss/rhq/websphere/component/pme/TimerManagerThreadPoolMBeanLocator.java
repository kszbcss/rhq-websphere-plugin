package be.fgov.kszbcss.rhq.websphere.component.pme;

import java.util.Map;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class TimerManagerThreadPoolMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final String jndiName;

    public TimerManagerThreadPoolMBeanLocator(String jndiName) {
        super("WebSphere", false);
        this.jndiName = jndiName;
    }

    @Override
    protected void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException, InterruptedException {
        ApplicationServer server = (ApplicationServer)processInfo.getServer();
        String name = server.queryConfig(new TimerManagerMapQuery(server.getNode(), server.getServer()), false).get(jndiName);
        if (name == null) {
            throw new JMException("No timer manager found for JNDI name " + jndiName);
        }
        props.put("type", "ThreadPool");
        props.put("name", name + ".Alarm Pool");
    }
    
    // TODO: implement equals and hashCode

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + jndiName + ")";
    }
}
