package be.fgov.kszbcss.websphere.rhq;

import java.lang.reflect.Method;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatsDynaBean implements DynaBean {
    private static final Log log = LogFactory.getLog(StatsDynaBean.class);
    
    private final Object stats;
    private final Method getStatisticMethod;

    public StatsDynaBean(Object stats) {
        this.stats = stats;
        try {
            getStatisticMethod = stats.getClass().getMethod("getStatistic", String.class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Argument doesn't appear to be a javax.management.j2ee.statistics.Stats object");
        }
    }

    public Object get(String name) {
        try {
            return getStatisticMethod.invoke(stats, name);
        } catch (Exception ex) {
            log.error("Unable to invoke getStatistic with statisticName=" + name);
            return null;
        }
    }

    public boolean contains(String name, String key) {
        throw new UnsupportedOperationException();
    }

    public Object get(String name, int index) {
        throw new UnsupportedOperationException();
    }

    public Object get(String name, String key) {
        throw new UnsupportedOperationException();
    }

    public DynaClass getDynaClass() {
        throw new UnsupportedOperationException();
    }

    public void remove(String name, String key) {
        throw new UnsupportedOperationException();
    }

    public void set(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void set(String name, int index, Object value) {
        throw new UnsupportedOperationException();
    }

    public void set(String name, String key, Object value) {
        throw new UnsupportedOperationException();
    }
}
