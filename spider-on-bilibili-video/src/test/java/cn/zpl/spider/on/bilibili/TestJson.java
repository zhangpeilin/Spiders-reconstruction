package cn.zpl.spider.on.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public class TestJson {

    public static void main(String[] args) {
        Body body = new Body();
        body.setBATCHNO("123456");
        System.out.println(JSON.toJSONString(body));

    }
}
@Data
class Body{
    @JsonProperty("batch_no")
            @JSONField(name = "batch_no")
    String BATCHNO;
}