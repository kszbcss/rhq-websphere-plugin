package be.fgov.kszbcss.rhq.websphere;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ModuleInfo implements Serializable {
    private static final long serialVersionUID = -4670813651457441679L;
    
    private final ModuleType type;
    private final String name;
    private final byte[] deploymentDescriptor;
    
    public ModuleInfo(ModuleType type, String name, byte[] deploymentDescriptor) {
        this.type = type;
        this.name = name;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public ModuleType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public Document getDeploymentDescriptor() {
        // TODO: we may want to cache this
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });
            return builder.parse(new InputSource(new ByteArrayInputStream(deploymentDescriptor)));
        } catch (Exception ex) {
            throw new Error(ex); // TODO
        }
    }
}
