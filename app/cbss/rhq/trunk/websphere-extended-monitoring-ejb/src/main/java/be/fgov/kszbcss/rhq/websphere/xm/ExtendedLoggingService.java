package be.fgov.kszbcss.rhq.websphere.xm;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.ibm.websphere.logging.WsLevel;

public class ExtendedLoggingService extends Handler {
    @Override
    public void publish(LogRecord record) {
        Level level = record.getLevel();
        if (level.intValue() >= WsLevel.AUDIT.intValue()) {
            new ExtendedLogMessage(level.getName(),
                    record.getLoggerName(), null, null, null, null);
            
        }
    }

    @Override
    public void flush() {
        
    }

    @Override
    public void close() throws SecurityException {
    }
}
