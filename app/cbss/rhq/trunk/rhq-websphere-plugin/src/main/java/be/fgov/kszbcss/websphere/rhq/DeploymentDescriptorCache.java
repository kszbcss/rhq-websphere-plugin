package be.fgov.kszbcss.websphere.rhq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.management.JMException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import be.fgov.kszbcss.websphere.rhq.mbean.MBean;

import com.ibm.websphere.management.exception.ConnectorException;

public class DeploymentDescriptorCache {
    private static final Log log = LogFactory.getLog(DeploymentDescriptorCache.class);
    
    private final MBean mbean;
    private long timestamp;
    private Document content;
    
    public DeploymentDescriptorCache(MBean mbean) {
        this.mbean = mbean;
    }

    public synchronized Document getContent() throws JMException, ConnectorException {
        if (content == null || System.currentTimeMillis() - timestamp > 10*60*1000) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to load deployment descriptor from " + mbean.getLocator());
            }
            String xml = (String)mbean.getAttribute("deploymentDescriptor");
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                builder.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        return new InputSource(new ByteArrayInputStream(new byte[0]));
                    }
                });
                content = builder.parse(new InputSource(new StringReader(xml)));
            } catch (Exception ex) {
                throw new Error(ex); // TODO
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully loaded deployment descriptor from " + mbean.getLocator());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using cached version of deployment descriptor for " + mbean.getLocator());
            }
        }
        return content;
    }
}
