package com.ibm.websphere.security.auth;

import javax.security.auth.Subject;

import com.ibm.websphere.security.WSSecurityException;

public class WSSubject {
    public static Subject getRunAsSubject() throws WSSecurityException {
        return null;
    }
    
    public static void setRunAsSubject(Subject subject) throws WSSecurityException {
        
    }
}
