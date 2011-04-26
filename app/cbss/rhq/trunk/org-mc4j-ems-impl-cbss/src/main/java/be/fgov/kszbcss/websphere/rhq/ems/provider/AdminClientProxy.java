package be.fgov.kszbcss.websphere.rhq.ems.provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.security.auth.Subject;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.StatsProxy;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.security.auth.WSSubject;

public class AdminClientProxy implements InvocationHandler, StatsProxy {
    private final AtomicLong failures = new AtomicLong();
    private final AtomicLong roundTrips = new AtomicLong();
    private final AdminClient adminClient;
    private final Subject subject;
    private final Map<Method,Method> methodMap = new HashMap<Method,Method>();
    
    public AdminClientProxy(AdminClient adminClient, Subject subject) {
        this.adminClient = adminClient;
        this.subject = subject;
        for (Method method : MBeanServer.class.getMethods()) {
            try {
                methodMap.put(method, AdminClient.class.getMethod(method.getName(), method.getParameterTypes()));
            } catch (NoSuchMethodException ex) {
                // Do nothing
            }
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = methodMap.get(method);
        if (targetMethod == null) {
            throw new RuntimeException("No corresponding method for " + method);
        } else {
            boolean failure = true;
            WSSubject.setRunAsSubject(subject);
            try {
                Object result = targetMethod.invoke(adminClient, args);
                failure = false;
                return result;
            } finally {
                if (failure) {
                    failures.incrementAndGet();
                }
                roundTrips.incrementAndGet();
                WSSubject.setRunAsSubject(null);
            }
        }
    }

    public MBeanServer buildServerProxy() {
        return (MBeanServer)Proxy.newProxyInstance(AdminClientProxy.class.getClassLoader(),
                new Class<?>[] { MBeanServer.class }, this);
    }

    public long getFailures() {
        return failures.get();
    }

    public long getRoundTrips() {
        return roundTrips.get();
    }
}
