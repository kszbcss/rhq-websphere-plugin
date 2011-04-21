package be.fgov.kszbcss.websphere.rhq;

import org.mc4j.ems.connection.bean.EmsBeanName;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class JDBCProviderDiscoveryComponent extends WebSphereMBeanResourceDiscoveryComponent<MBeanResourceComponent> {
    @Override
    protected String getResourceKey(EmsBeanName objectName) {
        // It is actually possible to define JDBC providers with the same name at different scopes (and this is
        // even the case in a default WAS installation). They are represented by distinct MBeans, so that choosing
        // the "name" property as the resource key actually seems wrong. However, all these MBeans give access to
        // the same information. In particular, all data sources for a given provider name appear in the stats
        // object and the jdbcDataSources property, regardless of the scope at which they are defined.
        // TODO: this actually means that DataSourceDiscoveryComponent is useless and that we could simply filter by provider name
        return objectName.getKeyProperty("name");
    }
}
