package cn.zpl.dao.utils;


import cn.zpl.common.bean.RestResponse;

public class CommonUtils {

    public static RestResponse getRestResponse(boolean flag) {
        return flag ? RestResponse.ok() : RestResponse.fail();
    }
}
