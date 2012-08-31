package be.fgov.kszbcss.rhq.websphere.mbean;

/**
 * An {@link MBeanClient} proxy. This interface is implemented by all proxies returned by
 * {@link MBeanClient#getProxy(Class)}. It allows to "unwrap" the proxy and to access the underlying
 * {@link MBeanClient}.
 */
public interface MBeanClientProxy {
    /**
     * Get the {@link MBeanClient} for this proxy instance.
     * 
     * @return the client
     */
    MBeanClient getMBeanClient();
}
