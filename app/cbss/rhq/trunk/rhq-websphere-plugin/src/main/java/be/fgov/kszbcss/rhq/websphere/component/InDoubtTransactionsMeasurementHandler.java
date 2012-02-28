package be.fgov.kszbcss.rhq.websphere.component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.SimpleMeasurementHandler;

/**
 * {@link MeasurementHandler} implementation that counts in-doubt transactions. This class solves
 * the problem that a transaction can only be considered as in-doubt if it has been reported twice
 * by the server.
 */
public abstract class InDoubtTransactionsMeasurementHandler extends SimpleMeasurementHandler {
    private static final Log log = LogFactory.getLog(InDoubtTransactionsMeasurementHandler.class);
    
    private final Set<String> transactionIds = new HashSet<String>();
    
    @Override
    protected final Object getValue() throws Exception {
        int confirmedCount = 0;
        Set<String> newTransactionIds = getTransactionIds();
        for (Iterator<String> it = transactionIds.iterator(); it.hasNext(); ) {
            String id = it.next();
            if (!newTransactionIds.contains(id)) {
                it.remove();
                if (log.isDebugEnabled()) {
                    log.debug("Transaction " + id + " is no longer in-doubt");
                }
            }
        }
        for (String id : newTransactionIds) {
            if (transactionIds.add(id)) {
                // New transaction ID
                if (log.isDebugEnabled()) {
                    log.debug("New (unconfirmed) in-doubt transaction " + id);
                }
            } else {
                // The transaction ID was already contained in the set
                confirmedCount++;
            }
        }
        return confirmedCount;
    }
    
    protected abstract Set<String> getTransactionIds() throws Exception;
}
