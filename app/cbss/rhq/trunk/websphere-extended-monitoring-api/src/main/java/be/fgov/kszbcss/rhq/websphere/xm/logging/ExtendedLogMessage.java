package be.fgov.kszbcss.rhq.websphere.xm.logging;

import java.io.Serializable;

public final class ExtendedLogMessage implements Serializable {
    private static final long serialVersionUID = -5515760895763431771L;

    private long sequence;
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
    
    void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }

    public String getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getMessage() {
        return message;
    }
}
