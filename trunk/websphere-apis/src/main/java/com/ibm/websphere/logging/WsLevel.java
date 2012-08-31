package com.ibm.websphere.logging;

import java.util.logging.Level;

public class WsLevel extends Level {
    public static final Level AUDIT = null;
    public static final Level FATAL = null;

    protected WsLevel(String name, int value) {
        super(name, value);
    }
}
