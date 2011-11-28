package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;

public class SIBDestination implements Serializable {
    private static final long serialVersionUID = -5897283441401077236L;

    private final String busName;
    private final String destinationName;
    
    public SIBDestination(String busName, String destinationName) {
        this.busName = busName;
        this.destinationName = destinationName;
    }

    public String getBusName() {
        return busName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public String toString() {
        return busName + ":" + destinationName;
    }
}
