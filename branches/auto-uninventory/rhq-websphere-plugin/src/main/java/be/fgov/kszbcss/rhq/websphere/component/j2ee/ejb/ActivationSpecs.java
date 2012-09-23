package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.io.Serializable;
import java.util.Map;

public class ActivationSpecs implements Serializable {
    private static final long serialVersionUID = -2060357852908686592L;
    
    private final Map<String,ActivationSpecInfo> map;

    public ActivationSpecs(Map<String, ActivationSpecInfo> map) {
        this.map = map;
    }
    
    public ActivationSpecInfo getActivationSpec(String jndiName) {
        return map.get(jndiName);
    }
}
