import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class test {
    public static void main(String[] args) {
        {
            List<Map<String, Object>> list1 = new ArrayList<>();

            // 添加示例数据
            Map<String, Object> map1 = new HashMap<>();
            map1.put("agency_code", "005");
            map1.put("group_id", 1);
            list1.add(map1);

            Map<String, Object> map2 = new HashMap<>();
            map2.put("agency_code", "008");
            map2.put("group_id", 2);
            list1.add(map2);

            Map<String, Object> map3 = new HashMap<>();
            map3.put("agency_code", "001");
            map3.put("group_id", 2);
            list1.add(map3);
            Map<Object, List<Map<String, Object>>> collect = list1.stream().collect(Collectors.groupingBy(stringObjectMap -> stringObjectMap.get("group_id")));

            System.out.println(collect);
            // 按照 agency_code 进行排序
            collect.forEach((o, maps) -> maps.sort(Comparator.comparing(m -> m.get("agency_code").toString())));
            System.out.println(collect);
        }
        }
}
