package com.ibm.wsspi.pmi.stat;

import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public interface SPIRangeStatistic extends SPIStatistic, WSRangeStatistic {
    void set(long currentValue);
}
