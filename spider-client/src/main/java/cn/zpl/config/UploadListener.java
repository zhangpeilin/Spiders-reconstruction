package cn.zpl.config;

import cn.zpl.common.bean.ServerInfo;
import cn.zpl.dao.service.IServerInfoService;
import cn.zpl.util.CrudTools;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UploadListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    IServerInfoService infoService;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        new Thread(this::startListening).start();
    }
    public void startListening(){
        while (true) {
            try {
                log.debug("开始获取公网ip");
                // 发起网络请求获取包含公网 IP 的响应
                String ipResponse = getPublicIp();
                // 解析响应获取公网 IP
                String publicIp = extractPublicIp(ipResponse);
                String lastIP = getLastIP();
                if (!lastIP.equalsIgnoreCase(publicIp)) {
                    log.debug("公网ip发生变化，新ip为{}", publicIp);
                    ServerInfo info = new ServerInfo();
                    info.setIp(publicIp);
                    info.setId(String.valueOf(System.currentTimeMillis()));
                    infoService.saveOrUpdate(info);
                }
                TimeUnit.MINUTES.sleep(5);
            } catch (Exception ignored) {
            }
        }
    }

    private String getLastIP(){
        SqlSession sqlSession = openSession();
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement preparedStatement = sqlSession.getConnection().prepareStatement("SELECT * from server_info ORDER BY CAST(id as UNSIGNED) desc LIMIT 0,1")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData(); //获得结果集结构信息,元数据
            int columnCount = md.getColumnCount();   //获得列数
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), resultSet.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            sqlSession.close();
        }
        if (list.isEmpty()) {
            return "";
        }
        ServerInfo serverInfo = JSON.parseObject(JSON.toJSONString(list.get(0)), ServerInfo.class);
        return serverInfo.getIp();
    }

    private static String getPublicIp() throws IOException {
        // 发起 GET 请求获取包含公网 IP 的响应
        URL url = new URL("https://checkip.amazonaws.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        String response = br.readLine();
        br.close();

        return response;
    }

    private static String extractPublicIp(String response) {
        // 去除响应中的空格和换行符
        return response.trim();
    }

    private SqlSession openSession() {
        SqlSessionFactory sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        return sqlSessionFactory.openSession();
    }
}