package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class ActivationSpecInfo implements Serializable {
    private static final long serialVersionUID = -6772635832761203196L;
    
    private final String destinationJndiName;
    private final Map<String,Object> properties;

    public ActivationSpecInfo(String destinationJndiName, Map<String,Object> properties) {
        this.destinationJndiName = destinationJndiName;
        this.properties = properties;
    }

    public String getDestinationJndiName() {
        return destinationJndiName;
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString() {
        return "[destinationJndiName=" + destinationJndiName + ",properties=" + properties + "]";
    }
}
