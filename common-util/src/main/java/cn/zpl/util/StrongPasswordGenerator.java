package cn.zpl.util;

import java.security.SecureRandom;

public class StrongPasswordGenerator {
    private static final String UPPER_CASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?";

    public static String generateStrongPassword(int length) {
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        // 至少包含一个大写字母
        password.append(UPPER_CASE_CHARS.charAt(random.nextInt(UPPER_CASE_CHARS.length())));

        // 至少包含一个小写字母
        password.append(LOWER_CASE_CHARS.charAt(random.nextInt(LOWER_CASE_CHARS.length())));

        // 至少包含一个数字
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));

        // 至少包含一个特殊字符
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // 生成剩余字符
        for (int i = 0; i < length - 4; i++) {
            String chars = UPPER_CASE_CHARS + LOWER_CASE_CHARS + DIGITS + SPECIAL_CHARS;
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // 打乱密码中字符的顺序
        for (int i = password.length() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(j));
            password.setCharAt(j, temp);
        }

        return password.toString();
    }

    public static void main(String[] args) {
        int passwordLength = 12;
        String strongPassword = generateStrongPassword(passwordLength);
        System.out.println(strongPassword);
    }
}