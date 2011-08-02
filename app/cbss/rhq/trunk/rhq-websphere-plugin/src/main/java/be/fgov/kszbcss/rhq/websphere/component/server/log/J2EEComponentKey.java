package be.fgov.kszbcss.rhq.websphere.component.server.log;

/**
 * Identifies a J2EE application component (servlet or enterprise bean). This class is used when
 * dispatching log events to individual components.
 */
public class J2EEComponentKey {
    private final String applicationName;
    private final String moduleName;
    private final String componentName;
    
    public J2EEComponentKey(String applicationName, String moduleName, String componentName) {
        this.applicationName = applicationName;
        this.moduleName = moduleName;
        this.componentName = componentName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof J2EEComponentKey) {
            J2EEComponentKey other = (J2EEComponentKey)obj;
            return applicationName.equals(other.applicationName) && moduleName.equals(other.moduleName) && componentName.equals(other.componentName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31*31*applicationName.hashCode() + 31*moduleName.hashCode() + componentName.hashCode();
    }
}
