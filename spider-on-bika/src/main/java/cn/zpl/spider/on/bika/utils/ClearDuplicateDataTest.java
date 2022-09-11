package cn.zpl.spider.on.bika.utils;

import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.util.CrudTools;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class ClearDuplicateDataTest {


    @Resource
    CrudTools<BikaList> tools;

    @Test
    public void business() {
        ClearDuplicateDataTest clearDuplicateDataTest = new ClearDuplicateDataTest();

        while (domain() > 0) {
            log.debug("继续执行……");
        }
        clearDuplicateDataTest.domain();
    }

    public int domain() {
        List<BikaList> bikaLists = tools.commonApiQueryBySql("sql:select * from bika_list where  id in(select * from ((select id from bika_list group by id having count(id) > 1 limit 900)) t)", BikaList.class);
        Map<String, List<BikaList>> listMap = bikaLists.stream().collect(Collectors.groupingBy(BikaList::getId));
        List<LinkedHashMap<String, Object>> list = new ArrayList<>();
        String sql = null;
        for (Map.Entry<String, List<BikaList>> stringListEntry : listMap.entrySet()) {
            List<BikaList> bikas = stringListEntry.getValue();
            if (bikas.size() > 1) {
                //如果存在一条数据有description，那么其他的删除，否则随机保留一条
                Optional<BikaList> any = bikas.stream().filter(bikaList -> !StringUtils.isEmpty(bikaList.getDescription()) || !StringUtils.isEmpty(bikaList.getLocalPath())).findFirst();
                if (any.isPresent()) {
                    BikaList bikaList = any.get();
                    sql = "delete from bika_list where id = ? and uuid_str <> ?";
                    LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                    params.put("id", bikaList.getId());
                    params.put("uuid_str", bikaList.getUuidStr());
                    list.add(params);
                } else {
                    //如果存在标记删除的，则删除其他重复记录
                    Optional<BikaList> any1 = bikas.stream().filter(bikaList -> bikaList.getIsDeleted() != null && bikaList.getIsDeleted() == 1).findAny();
                    Optional<BikaList> any2 = bikas.stream().filter(bikaList -> bikaList.getIsDeleted() != null && bikaList.getIsDeleted() == 0).findAny();
                    if (any1.isPresent() || any2.isPresent()) {
                        BikaList bikaList = any1.orElseGet(any2::get);
                        sql = "delete from bika_list where id = ? and uuid_str <> ?";
                        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                        params.put("id", bikaList.getId());
                        params.put("uuid_str", bikaList.getUuidStr());
                        list.add(params);
                    }
                }
            }
        }
        try {
            if (sql != null) {
                RestResponse restResponse = tools.commonDelete(sql, list);
                log.debug(String.valueOf(restResponse));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bikaLists.size();
    }
}
