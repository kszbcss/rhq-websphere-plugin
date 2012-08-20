package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.regex.Pattern;

import org.w3c.dom.Document;

public class ApplicationSpecVersionMeasurementHandler extends SpecVersionMeasurementHandler {
    private static final Pattern publicIdPattern = Pattern.compile("-//Sun Microsystems, Inc\\.//DTD J2EE Application ([0-9]\\.[0-9])//EN");
    
    private final ApplicationComponent applicationComponent;
    
    public ApplicationSpecVersionMeasurementHandler(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    @Override
    protected Document getDeploymentDescriptor() throws InterruptedException {
        return applicationComponent.getApplicationInfo(false).getDeploymentDescriptor();
    }

    @Override
    protected Pattern[] getPublicIdPatterns() {
        return new Pattern[] { publicIdPattern };
    }
}
