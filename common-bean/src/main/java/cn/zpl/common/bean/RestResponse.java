package cn.zpl.common.bean;

import com.alibaba.fastjson.JSONObject;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;

public class RestResponse extends HashMap<String, Object> {

    private static final long serialVersionUID = -8768946805942663366L;



    public RestResponse msg(String msg) {
        this.put(AppConstant.MESSAGE, msg);
        return this;
    }

    public RestResponse item(Object item) {
        this.put(AppConstant.ITEM, item);
        return this;
    }

    public Object getItem() {
        return this.get(AppConstant.ITEM);
    }

    public <T> List<T> getList(Class<T> clazz){
        assert get(AppConstant.LIST) != null;
        return JSONObject.parseArray(JSONObject.toJSONString(get(AppConstant.LIST)), clazz);
    }

    public <T> T getObject(Class<T> clazz) {
        assert get(AppConstant.ITEM) != null;
        return JSONObject.parseObject(JSONObject.toJSONString(get((AppConstant.ITEM))), clazz);
    }

    public RestResponse list(List<?> list) {
        this.put(AppConstant.LIST, list);
        return this;
    }

    public static RestResponse ok() {
        RestResponse result = new RestResponse();
        result.put(AppConstant.ERROR, AppConstant.OK);
        return result;
    }

    public static RestResponse ok(String msg) {
        return ok().msg(msg);
    }

    public static RestResponse ok(@NonNull Object item) {
        return ok().item(item);
    }

    public static RestResponse ok(List<?> list) {
        return ok().list(list);
    }

    public static RestResponse fail() {
        RestResponse result = new RestResponse();
        result.put(AppConstant.ERROR, AppConstant.FAIL);
        return result;
    }

    /**
     * 判断error字段是否为0，0表示成功
     */
    public boolean isSuccess() {
        return get(AppConstant.ERROR).equals(AppConstant.OK);
    }

    public static RestResponse fail(String msg) {
        return fail().msg(msg);
    }

    public static RestResponse fail(int errcode) {
        RestResponse fail = fail();
        fail.put(AppConstant.ERROR, errcode);
        return fail;
    }
}
