package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Contains the configuration of a deployed application.
 */
public class ApplicationConfiguration implements Serializable {
    private static final long serialVersionUID = 3962483806663765684L;

    private final Map<String,List<Map<String,String>>> data;

    public ApplicationConfiguration(Map<String,List<Map<String,String>>> data) {
        this.data = data;
    }

    public Map<String,List<Map<String,String>>> getData() {
        return data;
    }
}
