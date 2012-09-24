package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.io.Serializable;

public class SIBGatewayLinkInfo implements Serializable {
    private static final long serialVersionUID = -6879948489456904577L;
    
    private final String id;
    private final String name;
    private final String targetUuid;
    
    public SIBGatewayLinkInfo(String id, String name, String targetUuid) {
        this.id = id;
        this.name = name;
        this.targetUuid = targetUuid;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetUuid() {
        return targetUuid;
    }
}
