package cn.zpl;

public class FileNameSimilarity {

    public static void main(String[] args) {  
        String fileName1 = "file_name_v1.txt";
        String fileName2 = "file_name_v2.txt";

        double similarity = calculateSimilarity(fileName1, fileName2);  
        System.out.println("The similarity between the file names is: " + similarity);  
    }  

    public static double calculateSimilarity(String fileName1, String fileName2) {  
        int maxLength = Math.max(fileName1.length(), fileName2.length());  
        if (maxLength == 0) return 1.0; // Both strings are empty  

        int editDistance = computeLevenshteinDistance(fileName1, fileName2);  
        return 1.0 - (double) editDistance / maxLength; // Similarity score between 0 and 1  
    }  

    private static int computeLevenshteinDistance(String s1, String s2) {  
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];  

        // Initialize the base cases  
        for (int i = 0; i <= s1.length(); i++) {  
            dp[i][0] = i; // Deleting all characters from s1  
        }  
        for (int j = 0; j <= s2.length(); j++) {  
            dp[0][j] = j; // Inserting all characters to s1  
        }  

        // Compute the distances  
        for (int i = 1; i <= s1.length(); i++) {  
            for (int j = 1; j <= s2.length(); j++) {  
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {  
                    dp[i][j] = dp[i - 1][j - 1]; // No operation needed  
                } else {  
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1,  // Deletion  
                                                   dp[i][j - 1] + 1), // Insertion  
                                                   dp[i - 1][j - 1] + 1); // Substitution  
                }  
            }  
        }  

        return dp[s1.length()][s2.length()];  
    }  
}