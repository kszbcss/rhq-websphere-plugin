package com.ibm.websphere.management.application.client;

import java.io.Serializable;

public abstract class AppDeploymentTask implements Serializable {
    public String getName() {
        return null;
    }
    
    public String[][] getTaskData() {
        return null;
    }
}
