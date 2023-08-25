package cn.zpl.common.bean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageComparator implements Comparator<String> {

    //如果符合指定条件时，判定o1小，那么返回-1，否则返回1
    @Override
    public int compare(@NotNull String o1, String o2) {
        float num1;
        float num2;
        if (o1.contains(".") || o2.contains(".")) {
            try {
                num1 = Float.parseFloat(o1);
                num2 = Float.parseFloat(o2);
            } catch (Exception e) {
                try {
                    num1 = Integer.parseInt(o1.substring(0, o1.indexOf(".")));
                    num2 = Integer.parseInt(o2.substring(0, o2.indexOf(".")));
                } catch (Exception exception) {
                    return 0;
                }
            }
        } else if (Pattern.matches("第\\d*话", o1)) {
            Pattern pattern = Pattern.compile("第(\\d*)话");
            Matcher matcher1 = pattern.matcher(o1);
            Matcher matcher2 = pattern.matcher(o2);
            if (matcher1.find() && matcher2.find()) {
                num1 = Float.parseFloat(matcher1.group(1));
                num2 = Float.parseFloat(matcher1.group(1));
            } else {
                num1 = num2 = 0;
            }
        } else {
            num1 = Integer.parseInt(o1);
            num2 = Integer.parseInt(o2);

        }
        return num1 <= num2 ? num1 == num2 ? 0 : -1 : 1;
    }
}