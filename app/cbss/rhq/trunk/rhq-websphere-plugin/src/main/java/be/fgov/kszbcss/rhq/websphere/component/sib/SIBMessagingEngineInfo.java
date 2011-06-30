package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.io.Serializable;

public class SIBMessagingEngineInfo implements Serializable {
    private static final long serialVersionUID = -2564282568558624434L;

    private final String name;
    private final String busName;
    
    public SIBMessagingEngineInfo(String name, String busName) {
        this.name = name;
        this.busName = busName;
    }

    public String getName() {
        return name;
    }

    public String getBusName() {
        return busName;
    }
}
