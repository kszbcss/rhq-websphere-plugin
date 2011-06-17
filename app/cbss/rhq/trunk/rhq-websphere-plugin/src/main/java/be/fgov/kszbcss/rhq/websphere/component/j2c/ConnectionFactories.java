package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.io.Serializable;

public class ConnectionFactories implements Serializable {
    private static final long serialVersionUID = 5114839953640295086L;
    
    private final ConnectionFactoryInfo[] connectionFactories;

    public ConnectionFactories(ConnectionFactoryInfo[] connectionFactories) {
        this.connectionFactories = connectionFactories;
    }
    
    public String[] getJndiNames() {
        String[] result = new String[connectionFactories.length];
        for (int i=0; i<connectionFactories.length; i++) {
            result[i] = connectionFactories[i].getJndiName();
        }
        return result;
    }
    
    public ConnectionFactoryInfo getByJndiName(String jndiName) {
        for (ConnectionFactoryInfo cf : connectionFactories) {
            if (cf.getJndiName().equals(jndiName)) {
                return cf;
            }
        }
        return null;
    }
}
