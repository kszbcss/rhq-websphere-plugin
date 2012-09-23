package be.fgov.kszbcss.rhq.websphere.component.server.log.xm4was;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LogMessageTest {
    @Test
    public void testDecoding() {
        LogMessage message = new LogMessage("[1332841749344:900:1332851446269:test.TestListener:test_context_war:test_context.war:]Test warning");
        assertEquals(1332841749344L, message.getSequence());
        assertEquals(900, message.getLevel());
        assertEquals(1332851446269L, message.getTimestamp());
        assertEquals("test.TestListener", message.getLoggerName());
        assertEquals("test_context_war", message.getApplicationName());
        assertEquals("test_context.war", message.getModuleName());
        assertNull(message.getComponentName());
        assertEquals("Test warning", message.getMessage());
    }
}
