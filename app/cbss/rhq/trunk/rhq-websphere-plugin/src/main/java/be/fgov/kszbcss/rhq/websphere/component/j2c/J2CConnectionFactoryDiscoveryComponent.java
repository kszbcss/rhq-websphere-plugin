package be.fgov.kszbcss.rhq.websphere.component.j2c;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryDiscoveryComponent;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryType;

public class J2CConnectionFactoryDiscoveryComponent extends ConnectionFactoryDiscoveryComponent {
    @Override
    protected ConnectionFactoryType getType() {
        return ConnectionFactoryType.J2C;
    }

    @Override
    protected String getDescription() {
        return "A J2C Connection Factory";
    }
}
