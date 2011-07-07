package be.fgov.kszbcss.rhq.websphere.component.j2c;

import java.io.Serializable;

public class ConnectionFactories implements Serializable {
    private static final long serialVersionUID = 5114839953640295086L;
    
    private final J2CConnectionFactoryInfo[] connectionFactories;

    public ConnectionFactories(J2CConnectionFactoryInfo[] connectionFactories) {
        this.connectionFactories = connectionFactories;
    }
    
    public String[] getJndiNames() {
        String[] result = new String[connectionFactories.length];
        for (int i=0; i<connectionFactories.length; i++) {
            result[i] = connectionFactories[i].getJndiName();
        }
        return result;
    }
    
    public J2CConnectionFactoryInfo getByJndiName(String jndiName) {
        for (J2CConnectionFactoryInfo cf : connectionFactories) {
            if (cf.getJndiName().equals(jndiName)) {
                return cf;
            }
        }
        return null;
    }
}
