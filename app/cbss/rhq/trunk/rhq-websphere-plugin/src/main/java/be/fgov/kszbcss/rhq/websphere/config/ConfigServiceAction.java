package be.fgov.kszbcss.rhq.websphere.config;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

interface ConfigServiceAction<T> {
    T execute(ConfigService configService, Session session) throws JMException, ConnectorException;
}