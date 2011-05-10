package be.fgov.kszbcss.websphere.rhq;

import org.rhq.core.pluginapi.inventory.ResourceContext;

public class ApplicationComponent extends StatsEnabledMBeanResourceComponent<WebSphereServerComponent> {
    @Override
    public void start(ResourceContext<WebSphereServerComponent> context) {
        super.start(context);
        // TODO: getMBean() may fail if the server is unavailable
        context.getParentResourceComponent().getServer().registerStateChangeEventContext(getMBean(), context.getEventContext());
    }

    @Override
    public void stop() {
        super.stop();
    }
}
