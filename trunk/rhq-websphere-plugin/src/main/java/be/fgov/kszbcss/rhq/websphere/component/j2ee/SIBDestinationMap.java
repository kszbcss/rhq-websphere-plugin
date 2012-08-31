package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;
import java.util.Map;

/**
 * Maps JNDI names to SIB destinations.
 */
public class SIBDestinationMap implements Serializable {
    private static final long serialVersionUID = 8757662576395051692L;
    
    private final Map<String,SIBDestination> map;
    
    SIBDestinationMap(Map<String,SIBDestination> map) {
        this.map = map;
    }
    
    /**
     * Get the SIB destination for a given JNDI name;
     * 
     * @param jndiName
     *            the JNDI name
     * @return the information about the SIB destination or <code>null</code> if the JNDI name is
     *         not bound to a SIB destination
     */
    public SIBDestination getSIBDestination(String jndiName) {
        return map.get(jndiName);
    }
}
