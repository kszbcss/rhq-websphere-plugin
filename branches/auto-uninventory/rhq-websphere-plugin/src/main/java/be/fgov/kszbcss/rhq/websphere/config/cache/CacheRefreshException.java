package be.fgov.kszbcss.rhq.websphere.config.cache;

public class CacheRefreshException extends Exception {
    private static final long serialVersionUID = -523267662239503654L;

    public CacheRefreshException() {
    }

    public CacheRefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheRefreshException(String message) {
        super(message);
    }

    public CacheRefreshException(Throwable cause) {
        super(cause);
    }
}
