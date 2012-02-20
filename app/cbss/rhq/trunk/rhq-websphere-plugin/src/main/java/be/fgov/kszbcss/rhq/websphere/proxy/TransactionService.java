package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

public interface TransactionService {
    String[] listImportedPreparedTransactions() throws JMException, ConnectorException;
    String[] listManualTransactions() throws JMException, ConnectorException;
    String[] listRetryTransactions() throws JMException, ConnectorException;
    String[] listHeuristicTransactions() throws JMException, ConnectorException;
}
