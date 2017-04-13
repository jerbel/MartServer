package org.occiware.mart.server.parser.text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cgourdin on 12/04/2017.
 * This object gives a map for header rendering (used only with text/occi).
 */
public class HeaderPojo {

    private Map<String, List<String>> headerMap = new LinkedHashMap<>();

    public HeaderPojo(Map<String, List<String>> headerMap) {
        this.headerMap = headerMap;
    }

    public Map<String, List<String>> getHeaderMap() {
        return headerMap;
    }

    public void put(final String key, final String value) {
        if (headerMap.get(key) != null) {
            List<String> values = headerMap.get(key);
            if (!values.isEmpty() && !values.contains(value)) {
                values.add(value);
            }
        }
    }

    public void put(final String key, final List<String> values) {
        if (headerMap.containsKey(key)) {
            for (String value : values) {
                put(key, value);
            }
        } else {
            headerMap.put(key, values);
        }

    }

}