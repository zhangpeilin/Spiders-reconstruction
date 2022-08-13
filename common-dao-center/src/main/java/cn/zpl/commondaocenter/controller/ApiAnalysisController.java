package cn.zpl.commondaocenter.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.commondaocenter.service.IBikaService;
import cn.zpl.commondaocenter.utils.SpringContext;
import cn.zpl.config.UrlConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.base.CaseFormat;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
@RequestMapping("common/dao")
public class ApiAnalysisController {


    @Resource
    IBikaService bikaService;

    @Resource
    UrlConfig config;
    public static Set<Class<? extends Serializable>> entityList = new HashSet<>();

    //路径规则：entity代表要查询的实体对象
    ///api/mofdiv?size=999999&fetchProperties=*,parent[id,name,code,lastModifiedVersion]&sort=code,asc
    @PostMapping("/api/save")
    @SuppressWarnings("unchecked")
    public <T> RestResponse  commonEntitySave(@RequestBody JSONObject requestJson) {
        String entity = requestJson.getString("entity");
        JSONObject data = requestJson.getJSONObject("data");
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
                flag = iService.saveBatch(serializables);
            }
            if (serializable != null) {
                flag = iService.save(serializable);
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
        log.debug(entity);
        log.debug(fetchProperties);
        log.debug(condition);
        log.debug(String.valueOf(size));
        IService<Object> iService = (IService<Object>) SpringContext.getBeanDefinitionName(entity);
        if (iService == null) {
            log.error("未找到service类");
            return RestResponse.fail("未找到service类");
        }
        Pattern compile = Pattern.compile("[^=\\[\\],']+");
        if ("[*]".equals(condition)) {
            if (page == null) {
                page = new Page<>(0, 100);
            }
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
            objectQueryWrapper.and(wrapper -> wrapper.eq(key, value));
        }
        List<String> columns = new ArrayList<>();
        while (columnMatcher.find()) {
            columns.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, columnMatcher.group()));
        }
        //如果不为空，则拼接列名
        if (!columns.isEmpty()) {
            objectQueryWrapper.select(columns.toArray(new String[0]));
        }
        List<Object> list = iService.list(objectQueryWrapper);
        return RestResponse.ok().list(list);
    }

    private boolean checkEntityExists(String entity) {
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
        if (first.isPresent()) {
            aClass = first.get();
        } else {
            return true;
        }
        return false;
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
}
