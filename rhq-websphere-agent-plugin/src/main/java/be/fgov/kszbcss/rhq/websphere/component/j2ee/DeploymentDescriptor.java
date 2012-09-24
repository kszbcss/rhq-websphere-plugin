package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DeploymentDescriptor implements Serializable {
    private static final long serialVersionUID = 3438763050160572996L;

    private final byte[] content;

    public DeploymentDescriptor(byte[] content) {
        this.content = content;
    }
    
    public Document getDOM() {
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
            return builder.parse(new InputSource(new ByteArrayInputStream(content)));
        } catch (Exception ex) {
            throw new Error(ex); // TODO
        }
    }
}
