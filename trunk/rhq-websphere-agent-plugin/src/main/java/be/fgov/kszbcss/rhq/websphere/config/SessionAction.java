package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * An action that is executed in the context of a {@link Session}. This applies to methods of the
 * <tt>ConfigService</tt> and <tt>AppManagement</tt> MBeans. Note that the workspace ID used by the
 * <tt>AppManagement</tt> API can be derived from the {@link Session} object by calling
 * {@link Session#toString()} (it is the concatenation of the user name and session ID).
 * 
 * @param <T>
 *            the result type for the action
 */
interface SessionAction<T> {
    T execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException;
}