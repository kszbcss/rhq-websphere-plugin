package be.fgov.kszbcss.rhq.websphere.connector;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class AdminClientStatsCollector {
    public static final AdminClientStatsCollector INSTANCE = new AdminClientStatsCollector();
    
    private final ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();
    private AdminClientStats stats = new AdminClientStats();
    
    private AdminClientStatsCollector() {}
    
    public void addData(String destination, long nanos) {
        ReadLock readLock = statsLock.readLock();
        readLock.lock();
        try {
            stats.addData(destination, nanos);
        } finally {
            readLock.unlock();
        }
    }
    
    public AdminClientStats rotateStats() {
        WriteLock writeLock = statsLock.writeLock();
        writeLock.lock();
        try {
            AdminClientStats result = stats;
            stats = new AdminClientStats();
            result.end();
            return result;
        } finally {
            writeLock.unlock();
        }
    }
}
