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
        return "You must use the IBM JVM for MC4J when connection to WebSphere 5.x. The Sun JVM " +
            "can only be used for WS 6.";
    }

    public String[] getConnectionClasspathEntries() {
        return new String[] {
            "AppServer/lib/*",
            "AppServer/deploytool/itp/plugins/com.ibm.etools.jsse/ibmjsse.jar",
            "AppServer/java/jre/lib/ext/mail.jar",
            "AppServer/java/jre/lib/ibmcertpathprovider.jar",
            "AppServer/java/jre/lib/ext/ibmjceprovider.jar",
            "AppServer/deploytool/itp/plugins/org.apache.xerces_4.0.13/xercesImpl.jar",
            "AppServer/deploytool/itp/plugins/org.apache.xerces_4.0.13/xmlParserAPIs.jar",
        };
    }

    public String getConnectionNodeClassName() {
        return "org.mc4j.ems.impl.jmx.connection.support.providers.WebsphereConnectionProvider";
    }

    public String getDisplayName() {
        return "WebSphere 5.x+";
    }

    public String getRecongnitionPath() {
        return "AppServer/lib/runtime.jar";
    }
}
