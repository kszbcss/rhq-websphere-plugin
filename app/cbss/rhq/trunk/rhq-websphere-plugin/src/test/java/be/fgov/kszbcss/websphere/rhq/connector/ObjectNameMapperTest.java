package be.fgov.kszbcss.websphere.rhq.connector;

import static org.junit.Assert.assertEquals;

import javax.management.ObjectName;

import org.junit.Test;

public class ObjectNameMapperTest {
    @Test
    public void testMapRoutable() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("mycell", "mynode", "myserver");
        ObjectName serverObjectName = new ObjectName("MyDomain:type=SomeMBean,cell=mycell,node=mynode,process=myserver");
        ObjectName clientObjectName = mapper.toClientObjectName(serverObjectName);
        assertEquals(new ObjectName("MyDomain:type=SomeMBean"), clientObjectName);
        assertEquals(serverObjectName, mapper.toServerObjectName(clientObjectName));
    }
    
    @Test
    public void testMapNonRoutable() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("mycell", "mynode", "myserver");
        ObjectName name = new ObjectName("MyDomain:type=SomeMBean,name=test");
        assertEquals(name, mapper.toClientObjectName(name));
        assertEquals(name, mapper.toServerObjectName(name));
    }
    
    @Test
    public void testToServerObjectNameWithPropertyPattern() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("mycell", "mynode", "myserver");
        ObjectName name = new ObjectName("MyDomain:type=MyMBean,*");
        assertEquals(name, mapper.toServerObjectName(name));
    }
}
