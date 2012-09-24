package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;

import com.ibm.websphere.management.exception.ConnectorException;

public class ApplicationConfigurationQuery implements ConfigQuery<ApplicationConfiguration> {
    private static final long serialVersionUID = 8836539409900801046L;

    private final String applicationName;

    public ApplicationConfigurationQuery(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationConfiguration execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        return new ApplicationConfiguration(config.getApplicationInfo(applicationName));
    }

    @Override
    public int hashCode() {
        return applicationName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApplicationConfigurationQuery) {
            ApplicationConfigurationQuery other = (ApplicationConfigurationQuery)obj;
            return other.applicationName.equals(applicationName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + applicationName + ")";
    }
}
