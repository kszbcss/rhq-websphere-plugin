package be.fgov.kszbcss.rhq.websphere.xm;

import java.io.Serializable;

public class ExtendedLogMessage implements Serializable {
    private static final long serialVersionUID = -5515760895763431771L;

    private final String level;
    private final String loggerName;
    private final String applicationName;
    private final String moduleName;
    private final String componentName;
    private final String message;
    
    public ExtendedLogMessage(String level, String loggerName,
            String applicationName, String moduleName, String componentName,
            String message) {
        this.level = level;
        this.loggerName = loggerName;
        this.applicationName = applicationName;
        this.moduleName = moduleName;
        this.componentName = componentName;
        this.message = message;
    }
}
