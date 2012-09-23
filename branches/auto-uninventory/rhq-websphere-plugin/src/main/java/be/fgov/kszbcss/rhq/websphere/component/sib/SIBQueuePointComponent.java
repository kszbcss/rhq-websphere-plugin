package be.fgov.kszbcss.rhq.websphere.component.sib;

public class SIBQueuePointComponent extends SIBLocalizationPointComponent {
    @Override
    protected SIBLocalizationPointType getType() {
        return SIBLocalizationPointType.QUEUE;
    }
    
    @Override
    protected String getPMIModuleName() {
        return "Queues";
    }
}
