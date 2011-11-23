package be.fgov.kszbcss.rhq.websphere.config;

import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.configservice.ConfigServiceHelper;
import com.ibm.websphere.management.configservice.SystemAttributes;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Represents an object in the WebSphere configuration. This class provides a more convenient API
 * than {@link ConfigService}.
 */
public class ConfigObject {
    private final CellConfiguration config;
    private final ObjectName objectName;
    private AttributeList attributes;
    
    ConfigObject(CellConfiguration config, ObjectName objectName) {
        this.config = config;
        this.objectName = objectName;
    }
    
    public String getId() {
        return objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_ID);
    }
    
    public String getType() {
        return objectName.getKeyProperty(SystemAttributes._WEBSPHERE_CONFIG_DATA_TYPE);
    }
    
    private AttributeList getAttributes() throws JMException, ConnectorException {
        if (attributes == null) {
            attributes = config.execute(new SessionAction<AttributeList>() {
                public AttributeList execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException {
                    return configService.getAttributes(session, objectName, null, false);
                }
            });
        }
        return attributes;
    }
    
    public Object getAttribute(String name) throws JMException, ConnectorException {
        Object value = ConfigServiceHelper.getAttributeValue(getAttributes(), name);
        if (value instanceof ObjectName) {
            return new ConfigObject(config, (ObjectName)value);
        } else {
            return value;
        }
    }
    
    public List<ConfigObject> getChildren(String attributeName) throws JMException, ConnectorException {
        List<ConfigObject> children = new ArrayList<ConfigObject>();
        for (ObjectName objectName : (List<ObjectName>)getAttribute(attributeName)) {
            children.add(new ConfigObject(config, objectName));
        }
        return children;
    }
}
