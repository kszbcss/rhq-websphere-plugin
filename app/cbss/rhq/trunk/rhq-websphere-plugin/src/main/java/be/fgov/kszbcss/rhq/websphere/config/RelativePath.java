package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

class RelativePath extends Path {
    private final Path parent;
    private final String path;
    
    RelativePath(Path parent, String path) {
        this.parent = parent;
        this.path = path;
    }

    @Override
    ConfigObject[] resolveRelative(String relativePath) throws JMException, ConnectorException {
        if (relativePath == null) {
            return parent.resolveRelative(path);
        } else {
            return parent.resolveRelative(path + ":" + relativePath);
        }
    }
}
