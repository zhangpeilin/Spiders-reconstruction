package cn.zpl;
import cn.zpl.util.CommonIOUtils;

public class Test {
    public static void main(String[] args) {
        CommonIOUtils.testLog();
//        System.out.println("[sql:SELECT * from token ORDER BY CAST(id as UNSIGNED) desc LIMIT 0,1]".replaceAll("[{sql:}\\[\\]]", ""));
    }
}
