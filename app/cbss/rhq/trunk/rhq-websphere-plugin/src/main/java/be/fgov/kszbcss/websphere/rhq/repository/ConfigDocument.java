package be.fgov.kszbcss.websphere.rhq.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.ibm.websphere.management.repository.DocumentDigest;

public class ConfigDocument {
    private long lastUpdated;
    private DocumentDigest digest;
    private byte[] content;
    
    ConfigDocument() {}
    
    void update(DocumentDigest digest, byte[] content) {
        lastUpdated = System.currentTimeMillis();
        this.digest = digest;
        this.content = content;
    }

    long getLastUpdated() {
        return lastUpdated;
    }

    DocumentDigest getDigest() {
        return digest;
    }
    
    public InputStream getContent() {
        return new ByteArrayInputStream(content);
    }
}
