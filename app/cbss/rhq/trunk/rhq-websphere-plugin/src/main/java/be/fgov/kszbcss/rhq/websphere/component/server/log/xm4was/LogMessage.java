package be.fgov.kszbcss.rhq.websphere.component.server.log.xm4was;

public class LogMessage {
    private final long sequence;
    private final int level;
    private final long timestamp;
    private final String loggerName;
    private final String applicationName;
    private final String moduleName;
    private final String componentName;
    private final String message;
    
    public LogMessage(String formattedMessage) {
        int idx = formattedMessage.indexOf(']');
        String[] fields = formattedMessage.substring(1, idx).split(":", -1);
        sequence = Long.parseLong(fields[0]);
        level = Integer.parseInt(fields[1]);
        timestamp = Long.parseLong(fields[2]);
        loggerName = fields[3];
        applicationName = emptyToNull(fields[4]);
        moduleName = emptyToNull(fields[5]);
        componentName = emptyToNull(fields[6]);
        message = formattedMessage.substring(idx+1);
    }
    
    private static String emptyToNull(String s) {
        return s.length() == 0 ? null : s;
    }
    
    public long getSequence() {
        return sequence;
    }

    public int getLevel() {
        return level;
    }

    public long getTimestamp() {
        return timestamp;
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
