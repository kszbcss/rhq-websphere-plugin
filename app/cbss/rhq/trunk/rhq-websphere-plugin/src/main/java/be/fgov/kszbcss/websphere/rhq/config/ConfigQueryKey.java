package be.fgov.kszbcss.websphere.rhq.config;

import java.io.Serializable;

class ConfigQueryKey implements Serializable {
    private static final long serialVersionUID = -3046421288467157628L;
    
    private final String cell;
    private final ConfigQuery<?> query;
    
    public ConfigQueryKey(String cell, ConfigQuery<?> query) {
        this.cell = cell;
        this.query = query;
    }

    public String getCell() {
        return cell;
    }

    public ConfigQuery<?> getQuery() {
        return query;
    }

    @Override
    public int hashCode() {
        return 31*cell.hashCode() + query.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfigQueryKey) {
            ConfigQueryKey other = (ConfigQueryKey)obj;
            return other.cell.equals(cell) && other.query.equals(query);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "cell=" + cell + ",query=" + query;
    }
}
