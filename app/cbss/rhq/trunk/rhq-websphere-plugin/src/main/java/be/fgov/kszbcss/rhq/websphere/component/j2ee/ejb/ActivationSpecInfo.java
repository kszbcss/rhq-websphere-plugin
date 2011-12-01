package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.io.Serializable;

public class ActivationSpecInfo implements Serializable {
    private static final long serialVersionUID = -8038983088637379941L;
    
    private final String destinationJndiName;
    private final String busName;
    private final String destinationName;

    public ActivationSpecInfo(String destinationJndiName, String busName, String destinationName) {
        this.destinationJndiName = destinationJndiName;
        this.busName = busName;
        this.destinationName = destinationName;
    }

    public String getDestinationJndiName() {
        return destinationJndiName;
    }

    public String getBusName() {
        return busName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public String toString() {
        return "[destinationJndiName=" + destinationJndiName + ",busName=" + busName + ",destinationName=" + destinationName + "]";
    }
}
