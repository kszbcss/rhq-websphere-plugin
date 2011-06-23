package be.fgov.kszbcss.rhq.websphere.xm.logging;

import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.ibm.websphere.logging.WsLevel;
import com.ibm.ws.logging.TraceLogFormatter;
import com.ibm.ws.runtime.metadata.ApplicationMetaData;
import com.ibm.ws.runtime.metadata.ComponentMetaData;
import com.ibm.ws.runtime.metadata.ModuleMetaData;
import com.ibm.ws.threadContext.ComponentMetaDataAccessorImpl;

public class ExtendedLoggingService extends Handler {
    private final ComponentMetaDataAccessorImpl cmdAccessor = ComponentMetaDataAccessorImpl.getComponentMetaDataAccessor();
    private final ExtendedLogMessage[] buffer = new ExtendedLogMessage[1024];
    private int head;
    // We start at System.currentTimeMillis to make sure that the sequence is strictly increasing
    // event across a server restart
    private final long initialSequence;
    private long nextSequence;
    
    public ExtendedLoggingService() {
        initialSequence = System.currentTimeMillis();
        nextSequence = initialSequence;
    }
    
    @Override
    public void publish(LogRecord record) {
        int level = record.getLevel().intValue();
        if (level >= WsLevel.AUDIT.intValue()) {
            try {
                String applicationName;
                String moduleName;
                String componentName;
                ComponentMetaData cmd = cmdAccessor.getComponentMetaData();
                if (cmd == null) {
                    applicationName = null;
                    moduleName = null;
                    componentName = null;
                } else {
                    componentName = cmd.getName();
                    ModuleMetaData mmd = cmd.getModuleMetaData();
                    if (mmd == null) {
                        applicationName = null;
                        moduleName = null;
                    } else {
                        moduleName = mmd.getName();
                        ApplicationMetaData amd = mmd.getApplicationMetaData();
                        applicationName = amd == null ? null : amd.getName();
                    }
                }
                ExtendedLogMessage message = new ExtendedLogMessage(level, record.getMillis(),
                        record.getLoggerName(), applicationName, moduleName, componentName,
                        TraceLogFormatter.formatMessage(record, Locale.ENGLISH, TraceLogFormatter.UNUSED_PARM_HANDLING_APPEND_WITH_NEWLINE));
                synchronized (this) {
                    message.setSequence(nextSequence++);
                    buffer[head++] = message;
                    if (head == buffer.length) {
                        head = 0;
                    }
                }
            } catch (Throwable ex) {
                System.out.println("OOPS! Exception caught in logging handler");
                ex.printStackTrace(System.out);
            }
        }
    }

    public synchronized ExtendedLogMessage[] getMessages(long startSequence) {
        if (startSequence < initialSequence) {
            startSequence = initialSequence;
        }
        int bufferSize = buffer.length;
        int position;
        long longCount = nextSequence-startSequence;
        int count;
        if (longCount > bufferSize) {
            position = head;
            count = bufferSize;
        } else {
            count = (int)longCount;
            position = (head+bufferSize-count) % bufferSize;
        }
        ExtendedLogMessage[] messages = new ExtendedLogMessage[count];
        for (int i=0; i<count; i++) {
            messages[i] = buffer[position++];
            if (position == bufferSize) {
                position = 0;
            }
        }
        return messages;
    }
    
    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
