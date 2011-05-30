package be.fgov.kszbcss.websphere.rhq.connector.security;
import java.security.Provider;

public class CustomProvider extends Provider {
    public static final String NAME = "RHQWebSpherePlugin";
    
    public CustomProvider() {
        super(NAME, 0.0, "Custom security provider for the RHQ WebSphere plugin");
        setProperty("TrustManagerFactory.Dummy", DummyTrustManagerFactory.class.getName());
    }
}
