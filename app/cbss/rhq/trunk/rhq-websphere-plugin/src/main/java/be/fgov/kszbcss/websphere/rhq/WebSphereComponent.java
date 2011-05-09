package be.fgov.kszbcss.websphere.rhq;

import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.plugins.jmx.JMXComponent;

public interface WebSphereComponent<T extends ResourceComponent<?>> extends JMXComponent<T> {
    WebSphereServer getServer();
}
