package be.fgov.kszbcss.rhq.websphere.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.websphere.management.exception.ConnectorException;

class PathGroup extends Path {
    private final Path[] paths;
    
    PathGroup(Path... paths) {
        this.paths = paths;
    }

    @Override
    ObjectName[] resolveRelative(String relativePath) throws JMException, ConnectorException {
        List<ObjectName> result = new ArrayList<ObjectName>();
        for (Path path : paths) {
            result.addAll(Arrays.asList(path.resolveRelative(relativePath)));
        }
        return result.toArray(new ObjectName[result.size()]);
    }
}
