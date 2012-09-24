package be.fgov.kszbcss.rhq.websphere.component.sib;

public class SIBQueuePointDiscoveryComponent extends SIBLocalizationPointDiscoveryComponent {
    @Override
    protected SIBLocalizationPointType getType() {
        return SIBLocalizationPointType.QUEUE;
    }
}
