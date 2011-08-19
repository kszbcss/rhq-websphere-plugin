package be.fgov.kszbcss.rhq.websphere.xm.proc;

import java.io.File;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.xm.MBeanRegistrar;
import be.fgov.kszbcss.rhq.websphere.xm.Module;

import com.ibm.websphere.management.AdminService;
import com.ibm.websphere.management.AdminServiceFactory;
import com.ibm.wsspi.pmi.factory.StatsFactory;
import com.ibm.wsspi.pmi.factory.StatsFactoryException;
import com.ibm.wsspi.pmi.factory.StatsInstance;

public class ProcStatsModule implements Module {
    private static final Log log = LogFactory.getLog(ProcStatsModule.class);
    
    private static final File procDir = new File("/proc");
    
    private StatsInstance statsInstance;

    public boolean start(MBeanRegistrar mbeanRegistrar) {
        if (!procDir.exists()) {
            log.info(procDir + " doesn't exist on this platform");
            return true;
        }
        AdminService adminService = AdminServiceFactory.getAdminService();
        String pid;
        try {
            pid = (String)adminService.getAttribute(adminService.getLocalServer(), "pid");
        } catch (JMException ex) {
            log.error("Unable to determine the PID of the server process", ex);
            return false;
        }
        File fdDir = new File(new File(procDir, pid), "fd");
        if (!fdDir.exists()) {
            log.error(fdDir + " doesn't exist");
            return false;
        }
        try {
            statsInstance = StatsFactory.createStatsInstance("ProcStats", "/be/fgov/kszbcss/rhq/websphere/xm/ProcStats.xml", null, new ProcStatsCollector(fdDir));
        } catch (StatsFactoryException ex) {
            log.error("Failed to set up PMI statistics", ex);
            return false;
        }
        return true;
    }

    public void stop() {
        if (statsInstance != null) {
            try {
                StatsFactory.removeStatsInstance(statsInstance);
            } catch (StatsFactoryException ex) {
                log.error("Failed to remove stats instance", ex);
            }
        }
    }

}
