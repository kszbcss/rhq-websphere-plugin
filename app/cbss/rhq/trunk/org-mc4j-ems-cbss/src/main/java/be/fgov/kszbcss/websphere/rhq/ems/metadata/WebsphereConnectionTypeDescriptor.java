package be.fgov.kszbcss.websphere.rhq.ems.metadata;

import org.mc4j.ems.connection.support.metadata.AbstractConnectionTypeDescriptor;

public class WebsphereConnectionTypeDescriptor extends AbstractConnectionTypeDescriptor {
    private static final long serialVersionUID = -8429983695280640930L;

    public boolean isMEJBCompliant() {
        return true;
    }

    public String getDefaultServerUrl() {
        return "localhost:9100";
    }

    public String getDefaultPrincipal() {
        return "admin";
    }

    public String getDefaultCredentials() {
        return "";
    }

    public String getDefaultJndiName() {
        return null;
    }

    public String getDefaultInitialContext() {
        return null;
    }

    public String getConnectionType() {
        return "WebSphere";
    }

    public String getConnectionMessage() {
        return null;
    }

    public String[] getConnectionClasspathEntries() {
        return new String[] {
            "runtimes/com.ibm.ws.admin.client_7.0.0.jar",
        };
    }

    public String getConnectionNodeClassName() {
        return "be.fgov.kszbcss.websphere.rhq.ems.provider.WebsphereConnectionProvider";
    }

    public String getDisplayName() {
        return "WebSphere 7.0";
    }

    public String getRecongnitionPath() {
        return "runtimes/com.ibm.ws.admin.client_7.0.0.jar";
    }
}
