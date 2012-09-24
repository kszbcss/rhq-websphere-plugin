package be.fgov.kszbcss.rhq.websphere.component.j2c;

import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryComponent;
import be.fgov.kszbcss.rhq.websphere.component.ConnectionFactoryType;

public class J2CConnectionFactoryComponent extends ConnectionFactoryComponent {
    @Override
    protected ConnectionFactoryType getType() {
        return ConnectionFactoryType.J2C;
    }
}
