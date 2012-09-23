package be.fgov.kszbcss.rhq.websphere.component.xm4was;

public class ClassLoaderStatsComponent extends SingletonPMIModuleComponent {
    @Override
    protected String getPMIModuleName() {
        return "ClassLoaderStats";
    }
}
