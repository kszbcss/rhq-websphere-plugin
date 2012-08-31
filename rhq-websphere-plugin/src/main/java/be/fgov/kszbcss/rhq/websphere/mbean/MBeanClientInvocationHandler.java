package be.fgov.kszbcss.rhq.websphere.mbean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class MBeanClientInvocationHandler implements InvocationHandler {
    private final MBeanClient client;
    
    MBeanClientInvocationHandler(MBeanClient client) {
        this.client = client;
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        if (method.getDeclaringClass() == MBeanClientProxy.class) {
            return client;
        } else {
            Class<?>[] paramTypes = method.getParameterTypes();
            String[] signature = new String[paramTypes.length];
            for (int i=0; i<paramTypes.length; i++) {
                signature[i] = paramTypes[i].getName();
            }
            return client.invoke(method.getName(), params, signature);
        }
    }
}
