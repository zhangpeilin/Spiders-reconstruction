package cn.zpl.pojo;

import java.util.HashMap;
import java.util.function.IntFunction;

public class OrigionalDTO extends HashMap implements IntFunction {

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private String tableName;
    @Override
    public Object put(Object key, Object value) {
        if (value instanceof String) {
            return super.put(key, "true".equalsIgnoreCase((String) value) ? 1 :
                    "false".equalsIgnoreCase((String) value) ? 0 : value);
        }
        return super.put(key, value);
    }

    @Override
    public Object apply(int value) {
        return new OrigionalDTO[value];
    }

    public String getStr(Object key) {
        Object value = get(key);
        return value.toString();
    }
}
