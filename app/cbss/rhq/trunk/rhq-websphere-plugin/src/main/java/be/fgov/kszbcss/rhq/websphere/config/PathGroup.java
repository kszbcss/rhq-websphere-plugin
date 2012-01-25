package be.fgov.kszbcss.rhq.websphere.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

class PathGroup extends Path {
    private final Path[] paths;
    
    PathGroup(Path... paths) {
        this.paths = paths;
    }

    @Override
    ConfigObject[] resolveRelative(String relativePath) throws JMException, ConnectorException, InterruptedException {
        List<ConfigObject> result = new ArrayList<ConfigObject>();
        for (Path path : paths) {
            result.addAll(Arrays.asList(path.resolveRelative(relativePath)));
        }
        return result.toArray(new ConfigObject[result.size()]);
    }
}
