package be.fgov.kszbcss.websphere.rhq.connector;

import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.Subject;

import com.ibm.websphere.management.repository.RemoteInputStream;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class SecureRemoteInputStream extends InputStream {
    private final RemoteInputStream target;
    private final Subject subject;
    
    public SecureRemoteInputStream(RemoteInputStream target, Subject subject) {
        this.target = target;
        this.subject = subject;
    }
    
    private IOException toIOException(WSSecurityException ex) {
        IOException ioException = new IOException();
        ioException.initCause(ex);
        return ioException;
    }
    
    public int available() throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.available();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }

    public void close() throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.close();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }

    public void mark(int readlimit) {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.mark(readlimit);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean markSupported() {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.markSupported();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int read() throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.read();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.read(b, off, len);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }

    public int read(byte[] b) throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.read(b);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }

    public void reset() throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                target.reset();
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }

    public long skip(long n) throws IOException {
        try {
            WSSubject.setRunAsSubject(subject);
            try {
                return target.skip(n);
            } finally {
                WSSubject.setRunAsSubject(null);
            }
        } catch (WSSecurityException ex) {
            throw toIOException(ex);
        }
    }
}
