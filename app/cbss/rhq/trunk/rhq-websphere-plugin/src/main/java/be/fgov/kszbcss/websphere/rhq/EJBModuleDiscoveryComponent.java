package be.fgov.kszbcss.websphere.rhq;

public class EJBModuleDiscoveryComponent extends ModuleDiscoveryComponent  {
    @Override
    protected ModuleType getModuleType() {
        return ModuleType.EJB;
    }

    @Override
    protected String getDescription(String moduleName) {
        return "An EJB module.";
    }
}
