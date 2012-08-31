package com.ibm.ws.logging;

import java.util.Locale;
import java.util.logging.LogRecord;

public class TraceLogFormatter {
    public static final int UNUSED_PARM_HANDLING_DISCARD = 0;
    public static final int UNUSED_PARM_HANDLING_APPEND_WITH_SPACES = 1;
    public static final int UNUSED_PARM_HANDLING_APPEND_WITH_NEWLINE = 2;
    public static final int UNUSED_PARM_HANDLING_APPEND_WITH_PARM_LABELS = 3;
    
    public static final int UNUSED_PARM_HANDLING_APPEND_WITH_SEMICOLON_AND_COMMAS = 4;    public static String formatMessage(LogRecord logRecord, Locale locale, int unusedParmHandling) {
        return null;
    }
}
