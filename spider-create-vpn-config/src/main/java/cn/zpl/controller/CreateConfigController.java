package cn.zpl.controller;

import cn.zpl.common.bean.ServerInfo;
import com.alibaba.fastjson.JSON;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.reflections.Reflections;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class CreateConfigController {

    @Resource
    private ResourceLoader resourceLoader;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;
    @GetMapping("/getLastVpnConfig")
    public void getLastConfig(HttpServletResponse response) {
        String lastIP = getLastIP();
        try {
            String s = readFile("client.ovpn");
            s = s.replace("106.120.13.138", lastIP);
            try (BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {
                response.setHeader("Content-type", "octet-stream");
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("client.ovpn", "utf-8"));
                out.write(s.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
        } catch (IOException ignored) {
        }
    }

    @GetMapping("/testReflactions")
    public void testReflactions(){
        Set<Class<? extends Serializable>> entityList = new HashSet<>();
        if (entityList.isEmpty()) {
            Reflections reflections = new Reflections("cn.zpl.common.bean");
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
        }
    }

    public String readFile(String fileName) throws IOException {
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + fileName);
        byte[] fileData = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(fileData, StandardCharsets.UTF_8);
    }


    private String getLastIP(){
        SqlSessionFactory sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
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
}
