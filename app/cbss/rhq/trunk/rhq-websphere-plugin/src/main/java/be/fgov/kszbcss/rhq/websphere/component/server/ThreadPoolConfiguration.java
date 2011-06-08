package be.fgov.kszbcss.rhq.websphere.component.server;

import java.io.Serializable;

/**
 * Contains the information from a <tt>ThreadPool</tt> configuration object.
 */
public class ThreadPoolConfiguration implements Serializable {
    private static final long serialVersionUID = -1426730702832778076L;

    private final String name;
//    private final String mbeanIdentifier;
    
    public ThreadPoolConfiguration(String name/*, String mbeanIdentifier*/) {
        this.name = name;
//        this.mbeanIdentifier = mbeanIdentifier;
    }

    public String getName() {
        return name;
    }

//    public String getMbeanIdentifier() {
//        return mbeanIdentifier;
//    }
}
