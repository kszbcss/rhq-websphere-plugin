package be.fgov.kszbcss.rhq.websphere.component.j2ee;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains the configuration of a deployed application.
 */
public class ApplicationConfiguration implements Serializable {
    private static final long serialVersionUID = 3962483806663765684L;

    private final Map<String,List<Map<String,String>>> data;

    public ApplicationConfiguration(Map<String,List<Map<String,String>>> data) {
        this.data = data;
    }

    public Map<String,List<Map<String,String>>> getData() {
        return data;
    }
    
    public List<Map<String,String>> getData(String task, String module, String bean) {
        List<Map<String,String>> orgList = data.get(task);
        if (orgList == null) {
            return null;
        } else {
            List<Map<String,String>> filteredList = null;
            for (Map<String,String> entry : orgList) {
                // The data also has a column called "module", but it contains the display name of the
                // module, which is not what we use to identify a module. We extract the module name from
                // the uri, which is "<name>.war,WEB-INF/web.xml" or "<name>.jar,META-INF/ejb-jar.xml".
                String uri = entry.get("uri");
                int idx = uri.indexOf(',');
                if (module.equals(uri.substring(0, idx)) && (bean == null || bean.equals(entry.get("EJB")))) {
                    if (filteredList == null) {
                        filteredList = new ArrayList<Map<String,String>>();
                    }
                    filteredList.add(entry);
                }
            }
            return filteredList;
        }
    }
}
