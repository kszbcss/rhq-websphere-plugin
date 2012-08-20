package be.fgov.kszbcss.rhq.websphere.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class NamedThreadFactory implements ThreadFactory {
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
        return t;
    }
}
