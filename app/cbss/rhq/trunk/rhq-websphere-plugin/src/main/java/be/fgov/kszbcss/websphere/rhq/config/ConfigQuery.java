package be.fgov.kszbcss.websphere.rhq.config;

import java.io.Serializable;

import com.ibm.websphere.management.configservice.ConfigService;
import com.ibm.websphere.management.exception.ConfigServiceException;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Encapsulates a query for WebSphere configuration data. Implementations of this interface actually
 * plays two roles:
 * <ol>
 * <li>They are used as cache keys. This means that every implementation must correctly implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()}. In addition, they must be
 * serializable so that they can be stored in a persistent cache.
 * <li>They contain the logic to execute the query against a {@link ConfigService} instance.
 * </ol>
 * 
 * @param <T>
 *            the return type of the configuration data query
 */
public interface ConfigQuery<T extends Serializable> extends Serializable {
    T execute(ConfigService configService) throws ConfigServiceException, ConnectorException;
}
