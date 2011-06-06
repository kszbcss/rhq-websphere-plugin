package be.fgov.kszbcss.rhq.websphere.repository;

import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;

import com.ibm.websphere.management.repository.DocumentContentSource;
import com.ibm.websphere.management.repository.DocumentDigest;

public class ConfigDocumentFactory implements UpdatingCacheEntryFactory {
    private static final Log log = LogFactory.getLog(ConfigDocumentFactory.class);
    
    private final MBeanClient configRepositoryMBean;
    
    public ConfigDocumentFactory(MBeanClient configRepositoryMBean) {
        this.configRepositoryMBean = configRepositoryMBean;
    }

    public Object createEntry(Object key) throws Exception {
        ConfigDocument configDocument = new ConfigDocument();
        load((String)key, configDocument);
        return configDocument;
    }

    public void updateEntryValue(Object key, Object value) throws Exception {
        ConfigDocument configDocument = (ConfigDocument)value;
        // TODO: define constant here
        if (System.currentTimeMillis() - configDocument.getLastUpdated() > 300000) {
            DocumentDigest digest = (DocumentDigest)configRepositoryMBean.invoke("getDigest", new Object[] { key }, new String[] { String.class.getName() });
            if (!digest.equals(configDocument.getDigest())) {
                load((String)key, configDocument);
            }
        }
    }
    
    private void load(String key, ConfigDocument configDocument) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Loading " + key);
        }
        DocumentContentSource source = (DocumentContentSource)configRepositoryMBean.invoke("extract", new Object[] { key }, new String[] { String.class.getName() });
        configDocument.update(source.getDocument().getDigest(), IOUtils.toByteArray(source.getSource()));
    }
}
