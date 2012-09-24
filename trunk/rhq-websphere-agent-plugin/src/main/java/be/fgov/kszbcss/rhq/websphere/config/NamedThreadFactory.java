package be.fgov.kszbcss.rhq.websphere.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class NamedThreadFactory implements ThreadFactory, Thread.UncaughtExceptionHandler {
    private static final Log log = LogFactory.getLog(NamedThreadFactory.class);
    
    private final ThreadGroup group;
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    NamedThreadFactory(String namePrefix) {
        group = Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + "-" + threadNumber.getAndIncrement());
        t.setDaemon(false);
        t.setUncaughtExceptionHandler(this);
        return t;
    }

    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception on scheduled thread [" + t.getName() + "]", e);
    }
}
