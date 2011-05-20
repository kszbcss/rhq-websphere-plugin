package be.fgov.kszbcss.rhq.cert.util;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Provides common utility methods to make sure that the agent and server plugins use the same
 * conventions to derive the package name and version from the certificate.
 */
public class CertContentUtils {
    public static String getPackageName(X509Certificate cert) {
        return cert.getSubjectDN().toString();
    }
    
    public static String getVersion(X509Certificate cert) {
        StringBuilder buffer = new StringBuilder();

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(cert.getEncoded());
        } catch (Exception ex) {
            throw new Error("Unable to get digest from certificate");
        }
        byte[] digest = md.digest();
        
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) {
                buffer.append(':');
            }
            buffer.append(getHexDigit(((int)digest[i] & 0xf0) >> 4));
            buffer.append(getHexDigit((int)digest[i] & 0x0f));
        }
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        buffer.append(" (");
        buffer.append(format.format(cert.getNotBefore()));
        buffer.append(" - ");
        buffer.append(format.format(cert.getNotAfter()));
        buffer.append(")");
        
        return buffer.toString();
    }
    
    private static char getHexDigit(int nibble) {
        return (char)(nibble < 10 ? '0' + nibble : 'A' + nibble - 10);
    }
}
