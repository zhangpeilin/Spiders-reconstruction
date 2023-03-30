package cn.zpl.spider.on.bilibili.manga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.POST;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/9/29
 */
@SpringBootTest
public class TestRabbitMQTest {

    @Test
    public void main(){
        String str = "发送消息";
        String url = "http://localhost:8080/sendMsg/" + str;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, null, String.class);
        System.out.println(stringResponseEntity.getStatusCode());
    }
}
