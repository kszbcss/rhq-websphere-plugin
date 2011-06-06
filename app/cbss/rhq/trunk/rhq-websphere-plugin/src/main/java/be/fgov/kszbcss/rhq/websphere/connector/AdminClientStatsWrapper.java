package be.fgov.kszbcss.rhq.websphere.connector;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class AdminClientStatsWrapper extends AdminClientWrapper {
    private final AdminClientStatsCollector collector;
    
    public AdminClientStatsWrapper(AdminClient target, AdminClientStatsCollector collector) {
        super(target);
        this.collector = collector;
    }
    
    private StringBuilder formatObjectName(ObjectName objectName) {
        StringBuilder builder = new StringBuilder();
        builder.append(objectName.getDomain());
        builder.append(':');
        String type = objectName.getKeyProperty("type");
        builder.append(type == null ? "<unknown>" : type);
        return builder;
    }
    
    @Override
    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException, ConnectorException {
        long start = System.nanoTime();
        try {
            return super.getAttribute(name, attribute);
        } finally {
            long time = System.nanoTime()-start;
            collector.addData(formatObjectName(name).append('@').append(attribute).toString(), time);
        }
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException,
            ConnectorException {
        long start = System.nanoTime();
        try {
            return super.getAttributes(name, attributes);
        } finally {
            long time = System.nanoTime()-start;
            StringBuilder destination = formatObjectName(name);
            destination.append("@{");
            for (int i=0; i<attributes.length; i++) {
                if (i > 0) {
                    destination.append(',');
                }
                destination.append(attributes[i]);
            }
            destination.append('}');
            collector.addData(destination.toString(), time);
        }
    }

    @Override
    public Object invoke(ObjectName name, String operationName,
            Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, ConnectorException {
        long start = System.nanoTime();
        try {
            return super.invoke(name, operationName, params, signature);
        } finally {
            long time = System.nanoTime()-start;
            collector.addData(formatObjectName(name).append('#').append(operationName).toString(), time);
        }
    }
}
