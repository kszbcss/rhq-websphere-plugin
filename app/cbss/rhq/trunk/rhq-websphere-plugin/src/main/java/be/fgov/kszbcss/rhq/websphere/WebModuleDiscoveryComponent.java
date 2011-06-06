package be.fgov.kszbcss.rhq.websphere;

public class WebModuleDiscoveryComponent extends ModuleDiscoveryComponent {
    @Override
    protected ModuleType getModuleType() {
        return ModuleType.WEB;
    }

    @Override
    protected String getDescription(String moduleName) {
        return "A Web module.";
    }
}
