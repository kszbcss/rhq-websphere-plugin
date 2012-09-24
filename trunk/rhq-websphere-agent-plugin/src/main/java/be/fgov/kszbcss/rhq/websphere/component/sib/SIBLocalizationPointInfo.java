package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.io.Serializable;

public class SIBLocalizationPointInfo implements Serializable {
    private static final long serialVersionUID = -8832735831500379988L;

    private final SIBLocalizationPointType type;
    private final String destinationName;
    
    public SIBLocalizationPointInfo(SIBLocalizationPointType type, String destinationName) {
        this.type = type;
        this.destinationName = destinationName;
    }

    public SIBLocalizationPointType getType() {
        return type;
    }

    public String getDestinationName() {
        return destinationName;
    }
}
