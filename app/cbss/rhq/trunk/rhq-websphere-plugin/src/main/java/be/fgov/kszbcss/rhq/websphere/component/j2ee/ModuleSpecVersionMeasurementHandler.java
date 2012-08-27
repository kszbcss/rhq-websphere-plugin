package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.util.regex.Pattern;

import org.w3c.dom.Document;

import com.ibm.websphere.management.exception.ConnectorException;

public class ModuleSpecVersionMeasurementHandler extends SpecVersionMeasurementHandler {
    private static final Pattern publicIdPatterns[] = {
        Pattern.compile("-//Sun Microsystems, Inc\\.//DTD Enterprise JavaBeans ([0-9]\\.[0-9])//EN"),
        Pattern.compile("-//Sun Microsystems, Inc\\.//DTD Web Application ([0-9]\\.[0-9])//EN")
    };
    
    private final ModuleComponent moduleComponent;

    public ModuleSpecVersionMeasurementHandler(ModuleComponent moduleComponent) {
        this.moduleComponent = moduleComponent;
    }

    @Override
    protected Document getDeploymentDescriptor() throws InterruptedException, ConnectorException {
        return moduleComponent.getModuleInfo(false).getDeploymentDescriptor();
    }

    @Override
    protected Pattern[] getPublicIdPatterns() {
        return publicIdPatterns;
    }
}
