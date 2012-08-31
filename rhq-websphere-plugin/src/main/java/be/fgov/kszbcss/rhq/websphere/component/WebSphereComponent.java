package be.fgov.kszbcss.rhq.websphere.component;

import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jmx.JMXComponent;

import be.fgov.kszbcss.rhq.websphere.ApplicationServer;

public interface WebSphereComponent<T extends ResourceComponent<?>> extends JMXComponent<T> {
    ResourceContext<T> getResourceContext();
    ApplicationServer getServer();
}
