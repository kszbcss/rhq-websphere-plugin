package be.fgov.kszbcss.rhq.websphere;

public class PIDChangeTracker {
    private final PIDWatcher watcher;
    private boolean restarted;
    
    PIDChangeTracker(PIDWatcher watcher) {
        this.watcher = watcher;
    }
    
    public boolean isRestarted() {
        watcher.update();
        synchronized (this) {
            boolean result = restarted;
            restarted = false;
            return result;
        }
    }
    
    synchronized void setRestarted() {
        restarted = true;
    }
}
