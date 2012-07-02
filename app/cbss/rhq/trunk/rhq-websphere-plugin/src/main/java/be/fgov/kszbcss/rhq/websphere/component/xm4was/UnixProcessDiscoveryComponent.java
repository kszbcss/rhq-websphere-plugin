package be.fgov.kszbcss.rhq.websphere.component.xm4was;

public class UnixProcessDiscoveryComponent extends SingletonPMIModuleDiscoveryComponent {
    @Override
    protected String getPMIModuleName() {
        return "ProcStats";
    }

    @Override
    protected String getResourceDescription() {
        return "UNIX Process";
    }

    @Override
    protected String getResourceName() {
        return "UNIX Process Statistics";
    }
}
