package be.fgov.kszbcss.rhq.websphere.connector.ems;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.StatsProxy;

public class MBeanServerStatsProxy implements InvocationHandler, StatsProxy {
    private final AtomicLong failures = new AtomicLong();
    private final AtomicLong roundTrips = new AtomicLong();
    private final MBeanServer target;
    
    public MBeanServerStatsProxy(MBeanServer target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean failure = true;
        try {
            Object result = method.invoke(target, args);
            failure = false;
            return result;
        } finally {
            if (failure) {
                failures.incrementAndGet();
            }
            roundTrips.incrementAndGet();
        }
    }

    public MBeanServer buildServerProxy() {
        return (MBeanServer)Proxy.newProxyInstance(MBeanServerStatsProxy.class.getClassLoader(),
                new Class<?>[] { MBeanServer.class }, this);
    }

    public long getFailures() {
        return failures.get();
    }

    public long getRoundTrips() {
        return roundTrips.get();
    }
}
