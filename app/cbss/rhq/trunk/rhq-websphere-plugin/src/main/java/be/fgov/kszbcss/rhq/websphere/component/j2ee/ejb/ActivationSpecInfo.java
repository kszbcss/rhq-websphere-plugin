package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.io.Serializable;

public class ActivationSpecInfo implements Serializable {
    private static final long serialVersionUID = -2819387961280836772L;

    private final String destinationJndiName;

    public ActivationSpecInfo(String destinationJndiName) {
        this.destinationJndiName = destinationJndiName;
    }

    public String getDestinationJndiName() {
        return destinationJndiName;
    }

    @Override
    public String toString() {
        return "[destinationJndiName=" + destinationJndiName + "]";
    }
}
