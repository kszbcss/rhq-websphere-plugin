package com.ibm.websphere.pmi.stat;

public interface WSAverageStatistic extends WSStatistic {
    long getCount();
    
    long getTotal();
    
    double getMean();
    
    long getMin();
    
    long getMax();
    
    double getSumOfSquares();
}
