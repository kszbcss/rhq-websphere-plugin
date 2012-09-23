package be.fgov.kszbcss.rhq.websphere.component.server.log;

import org.apache.commons.lang.ObjectUtils;

/**
 * Identifies a J2EE application component (servlet or enterprise bean). This class is used when
 * dispatching log events to individual components. Note that servlet context listeners are not
 * considered as components, although they can produce log events. They are represented using an
 * instance with a null component name.
 */
public class J2EEComponentKey {
    private final String applicationName;
    private final String moduleName;
    private final String componentName;
    
    public J2EEComponentKey(String applicationName, String moduleName, String componentName) {
        if (applicationName == null || moduleName == null) {
            throw new IllegalArgumentException("applicationName and moduleName must not be null");
        }
        this.applicationName = applicationName;
        this.moduleName = moduleName;
        this.componentName = componentName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof J2EEComponentKey) {
            J2EEComponentKey other = (J2EEComponentKey)obj;
            return applicationName.equals(other.applicationName) && moduleName.equals(other.moduleName) && ObjectUtils.equals(componentName, componentName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31*31*applicationName.hashCode() + 31*moduleName.hashCode() + (componentName == null ? 0 : componentName.hashCode());
    }
}
