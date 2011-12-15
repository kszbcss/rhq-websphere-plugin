package be.fgov.kszbcss.rhq.websphere.component.xm;

public class ClassLoaderStatsDiscoveryComponent extends SingletonPMIModuleDiscoveryComponent {
    @Override
    protected String getPMIModuleName() {
        return "ClassLoaderStats";
    }

    @Override
    protected String getResourceDescription() {
        return "Class Loader Stats";
    }

    @Override
    protected String getResourceName() {
        return "Class Loader Statistics";
    }
}
