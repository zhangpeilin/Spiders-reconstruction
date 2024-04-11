package cn.zpl.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

public class FastJson {

    public JSONObject paraseJson(String string) {
        return JSON.parseObject(string);
    }

}
