package com.ibm.websphere.sib.admin;

import java.io.Serializable;

public interface SIBLinkReceiver extends Serializable {
    String getId();
    String getTargetUuid();
    String getState();
    String getForeignEngineUuid();
    long getTimeSinceLastMessageReceived();
    long getDepth();
    long getNumberOfMessagesReceived();
    String getReceiverType();
    String getTargetDestination();
}
