package be.fgov.kszbcss.rhq.websphere.xm;

public interface Module {
    boolean start(MBeanRegistrar mbeanRegistrar);
    void stop();
}
