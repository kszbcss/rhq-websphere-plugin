package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.sib.admin.SIBLinkReceiver;

public interface SIBGatewayLink {
    Boolean isActive() throws JMException, ConnectorException;
    SIBLinkReceiver[] listLinkReceivers() throws JMException, ConnectorException;
}
