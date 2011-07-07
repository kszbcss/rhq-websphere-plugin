package be.fgov.kszbcss.rhq.websphere.component.j2c;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryInfo;

public class J2CConnectionFactoryInfo extends ConnectionFactoryInfo {
    private static final long serialVersionUID = 116695055256735899L;

    public J2CConnectionFactoryInfo(String providerName, String name, String jndiName) {
        super(providerName, name, jndiName);
    }
}
