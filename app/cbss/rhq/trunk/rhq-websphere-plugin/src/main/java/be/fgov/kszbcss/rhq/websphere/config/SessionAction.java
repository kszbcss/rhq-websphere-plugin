package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.proxy.AppManagement;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * An action that is executed in the context of a {@link Session}. This applies to methods of the
 * <tt>ConfigService</tt> and <tt>AppManagement</tt> MBeans. Note that the workspace ID in the
 * <tt>AppManagement</tt> API is the same as the session ID, as implied by the following quote from
 * the WebSphere documentation:
 * <blockquote>
 * Every method on the AppMangement interface takes session ID (workspace ID) as the last parameter.
 * </blockquote>
 * 
 * @param <T>
 *            the result type for the action
 */
interface SessionAction<T> {
    T execute(ConfigService configService, AppManagement appManagement, Session session) throws JMException, ConnectorException;
}