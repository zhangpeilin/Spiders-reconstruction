package cn.zpl.dao.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.UrlConfig;
import cn.zpl.dao.mapper.OriSqlMapper;
import cn.zpl.dao.service.IBikaService;
import cn.zpl.dao.utils.SpringContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.base.CaseFormat;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionTemplate;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.Resource;
import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
@RequestMapping("common/dao")
public class ApiAnalysisController {


    @Resource
    IBikaService bikaService;

    @Resource
    OriSqlMapper oriSqlMapper;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    @Resource
    UrlConfig config;
    public static Set<Class<? extends Serializable>> entityList = new HashSet<>();

    public ThreadLocal<Class<?>> entityCache = new ThreadLocal<>();
    private static final LoadingCache<String, Class<?>> cache = CacheBuilder.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, Class<?>>() {
                @Override
                public Class<?> load(@NotNull String key) {
                    return getEntityExists(key);
                }
            });


    //路径规则：entity代表要查询的实体对象
    ///api/mofdiv?size=999999&fetchProperties=*,parent[id,name,code,lastModifiedVersion]&sort=code,asc
    @PostMapping("/api/save")
    @SuppressWarnings("unchecked")
    public <T> RestResponse commonEntitySave(@RequestBody JSONObject requestJson) {
        String entity = requestJson.getString("entity");
        Object data = requestJson.get("data");
        if (checkEntityExists(entity)) {
            return RestResponse.fail("找不到实体类");
        }
        if (entityList.isEmpty()) {
            Reflections reflections = new Reflections("cn.zpl.common.bean");
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
            reflections = new Reflections("BOOT-INF.classes.cn.zpl.commondaocenter.bean");
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
        }
        T serializable = null;
        Optional<Class<? extends Serializable>> first = entityList.stream().filter(clazz -> clazz.getSimpleName().equalsIgnoreCase(entity)).findFirst();
        if (first.isPresent()) {
            List<T> serializables = null;
            try {
                serializable = (T) JSON.parseObject(JSON.toJSONString(data), first.get());
            } catch (Exception e) {
                //如果解析失败尝试解析为array
                serializables = (List<T>) JSON.parseArray(JSON.toJSONString(data), first.get());
            }
            boolean flag = false;
            IService<T> iService = (IService<T>) SpringContext.getBeanDefinitionName(entity);
            assert iService != null;
            if (serializable == null && serializables != null) {
                flag = iService.saveOrUpdateBatch(serializables);
            }
            if (serializable != null) {
                flag = iService.saveOrUpdate(serializable);
            }
            return flag ? RestResponse.ok("保存成功") : RestResponse.fail("保存失败");
        } else {
            return RestResponse.fail("没有找到实体类");
        }
    }

    /**
     * 公共api解析器，获取实体名称和要查询的字段，条件
     *
     * @param entity          实体名，表名
     * @param fetchProperties 要查询的字段
     * @param size            查询数量
     * @return 返回json
     */
    @GetMapping("/api/query/{entity}")
    @SuppressWarnings("unchecked")
    public RestResponse apiAnalysis2(@PathVariable("entity") String entity, @RequestParam(value = "fetchProperties", required = false) String fetchProperties, @RequestParam(value = "condition", required = false) String condition, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "page", required = false) Page<Object> page) {
        if (checkEntityExists(entity)) {
            return RestResponse.fail("找不到实体类");
        }
        //预处理为空的标记为[*]
        if (StringUtils.isEmpty(fetchProperties)) {
            fetchProperties = config.getNothing();
        }
        if (StringUtils.isEmpty(condition)) {
            condition = config.getNothing();
        }
        if (page == null) {
            page = new Page<>(1, 20);
        }
        log.debug(entity);
        log.debug(fetchProperties);
        log.debug(condition);
        log.debug(String.valueOf(size));
//        IService<Object> iService = (IService<Object>) SpringContext.getBeanDefinitionName(entity);
        IService<Object> iService = null;
        try {
            iService = SpringContext.getBeanWithGenerics((Class<Object>) cache.get(entity));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (iService == null) {
            log.error("未找到service类");
            return RestResponse.fail("未找到service类");
        }
        Pattern compile = Pattern.compile("[^=\\[\\],']+");
        if (condition.startsWith("[sql:")) {
            List<Map<String, Object>> list = new ArrayList<>();
            SqlSession sqlSession = openSession();
            String sql = condition.replaceAll("\\[sql:|]", "");
            try (PreparedStatement preparedStatement = sqlSession.getConnection().prepareStatement(sql)) {
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
            return RestResponse.ok().list(list);
        }
        if ("[*]".equals(condition)) {
            iService.page(page);
            return RestResponse.ok().list(page.getRecords());
        }
        Matcher conditionMatcher = compile.matcher(condition);
        Matcher columnMatcher = compile.matcher(fetchProperties);
        QueryWrapper<Object> objectQueryWrapper = new QueryWrapper<>();
        while (conditionMatcher.find()) {
            log.debug(conditionMatcher.group());
            String key = conditionMatcher.group();
            if (!conditionMatcher.find()) {
                return RestResponse.fail("传入查询条件不完整");
            }
            log.debug(conditionMatcher.group());
            String value = conditionMatcher.group();
            objectQueryWrapper.and(wrapper -> wrapper.eq(key.trim(), value.trim()));
        }
        List<String> columns = new ArrayList<>();
        while (columnMatcher.find()) {
            columns.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, columnMatcher.group()));
        }
        //如果不为空，则拼接列名
        if (!columns.isEmpty()) {
            objectQueryWrapper.select(columns.toArray(new String[0]));
        }
        List<Object> list = iService.page(page, objectQueryWrapper).getRecords();
        return RestResponse.ok().list(list);
    }

    /**
     * 公共api解析器，获取实体名称和要查询的字段，条件
     *
     * @return 返回json
     */
    @PostMapping("/api/delete")
    public RestResponse delete(@RequestBody JSONObject requestJson) {
        //预处理为空的标记为[*]
        RestResponse ok = RestResponse.ok();
        String sql = requestJson.getString("sql");
        JSONArray objects = requestJson.getJSONArray("params");
        SqlSession sqlSession = openSession();
        try (PreparedStatement preparedStatement = sqlSession.getConnection().prepareStatement(sql)) {
            for (Object object : objects) {
                String[] paramsArray = object.toString().split(",");
                for (int i = 0; i < paramsArray.length; i++) {
                    preparedStatement.setObject(i + 1, paramsArray[i]);
                }
                preparedStatement.addBatch();
            }
            int[] i = preparedStatement.executeBatch();
            log.debug("删除条数：{}", i);
            ok.msg("删除条数：" + Arrays.stream(i).filter(value -> value == 1).count());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sqlSession.close();
        return ok;
    }

    @SneakyThrows
    private boolean checkEntityExists(String entity) {
        Class<?> aClass = cache.get(entity);
        return aClass == null;
    }

    private static Class<?> getEntityExists(String entity) {
        Class<?> aClass;
        if (entityList.isEmpty()) {
            Reflections reflections = new Reflections("cn.zpl.common.bean");
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
            reflections = new Reflections("BOOT-INF.classes.cn.zpl.commondaocenter.bean");
//            ConfigurationBuilder builder = new ConfigurationBuilder();
//            builder.addClassLoaders(this.getClass().getClassLoader());
//            builder.forPackage("cn.zpl.commondaocenter.bean", this.getClass().getClassLoader());
//            Reflections reflections1 = new Reflections(builder);
//            Set<Class<? extends Serializable>> subTypesOf = reflections1.getSubTypesOf(Serializable.class);
//            log.debug("找到的类：{}", subTypesOf);

//            subTypesOf.forEach(System.out::println);
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
        }
        Optional<Class<? extends Serializable>> first = entityList.stream().filter(clazz -> clazz.getSimpleName().equalsIgnoreCase(entity)).findFirst();
//        log.debug(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "PictureAnalyze"));
        return first.orElse(null);
    }

    @SneakyThrows
    @Deprecated
    private IService loadServiceByEntity(String entity) {

        Reflections reflections = new Reflections("classpath:\\cn.zpl.commondaocenter.service.impl");
        Set<Class<? extends IService>> serviceImplements = reflections.getSubTypesOf(IService.class);
        for (Class<? extends IService> serviceImplement : serviceImplements) {
            System.out.println(serviceImplement.getName());
        }
        Optional<Class<? extends IService>> first = serviceImplements.stream().filter(aClass -> {
            return !aClass.isInterface() && (aClass.getGenericSuperclass() instanceof ParameterizedType) && ((Class<?>) ((ParameterizedTypeImpl) aClass.getGenericSuperclass()).getActualTypeArguments()[1]).getSimpleName().equalsIgnoreCase(entity);
        }).findFirst();
        if (first.isPresent()) {
            Object bean = SpringContext.getBean(Introspector.decapitalize(ClassUtils.getShortName(first.get())));
            log.debug("注入的service：{}", bikaService);
            log.debug("自己解析的service：{}", bean);
            return (IService) bean;
        }
        return null;
    }

    private SqlSession openSession() {
        SqlSessionFactory sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        return sqlSessionFactory.openSession();
    }
}
