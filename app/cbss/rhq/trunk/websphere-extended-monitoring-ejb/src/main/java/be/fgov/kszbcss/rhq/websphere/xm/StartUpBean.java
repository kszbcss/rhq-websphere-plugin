package be.fgov.kszbcss.rhq.websphere.xm;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

@SuppressWarnings("serial")
public class StartUpBean implements SessionBean {
    public void ejbCreate() throws CreateException {
    }
    
    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
    }

    public boolean start() {
        
        
        return true;
    }

    public void stop() {
        
    }
}
