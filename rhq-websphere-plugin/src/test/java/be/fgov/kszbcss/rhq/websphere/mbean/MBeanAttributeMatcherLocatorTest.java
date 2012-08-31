package be.fgov.kszbcss.rhq.websphere.mbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.management.ObjectName;

import org.junit.Test;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanAttributeMatcherLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.MBeanLocator;

public class MBeanAttributeMatcherLocatorTest {
    @Test
    public void testEquals() throws Exception {
        MBeanLocator locator1 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        MBeanLocator locator2 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        assertTrue(locator1.equals(locator2));
    }

    @Test
    public void testHashCode() throws Exception {
        MBeanLocator locator1 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        MBeanLocator locator2 = new MBeanAttributeMatcherLocator(new ObjectName("WebSphere:type=DataSource,*"), "jndiName", "jdbc/test");
        assertEquals(locator1.hashCode(), locator2.hashCode());
    }
}
