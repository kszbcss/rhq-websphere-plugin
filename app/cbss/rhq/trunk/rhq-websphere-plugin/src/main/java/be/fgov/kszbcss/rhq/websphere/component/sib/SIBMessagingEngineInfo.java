package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SIBMessagingEngineInfo implements Serializable {
    private static final long serialVersionUID = 431849217227599418L;

    private final String name;
    private final String busName;
    private final SIBLocalizationPointInfo[] localizationPoints;
    
    public SIBMessagingEngineInfo(String name, String busName, SIBLocalizationPointInfo[] localizationPoints) {
        this.name = name;
        this.busName = busName;
        this.localizationPoints = localizationPoints;
    }

    public String getName() {
        return name;
    }

    public String getBusName() {
        return busName;
    }
    
    public String[] getDestinationNames(SIBLocalizationPointType type) {
        List<String> result = new ArrayList<String>();
        for (SIBLocalizationPointInfo localizationPoint : localizationPoints) {
            if (localizationPoint.getType().equals(type)) {
                result.add(localizationPoint.getDestinationName());
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
