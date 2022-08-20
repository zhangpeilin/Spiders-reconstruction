public class Test {
    public static void main(String[] args) {
        System.out.println("[sql:SELECT * from token ORDER BY CAST(id as UNSIGNED) desc LIMIT 0,1]".replaceAll("[{sql:}\\[\\]]", ""));
    }
}
