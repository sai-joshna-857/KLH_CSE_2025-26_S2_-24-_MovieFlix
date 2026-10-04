package com.movieflix.engine;

public class NeedlemanWunsch {
    public static int align(String s1, String s2, int gapPenalty) {
        int m = s1.length(), n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i * gapPenalty;
        for (int j = 0; j <= n; j++) dp[0][j] = j * gapPenalty;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int match = dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 1 : -1);
                int delete = dp[i - 1][j] + gapPenalty;
                int insert = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(match, Math.max(delete, insert));
            }
        }
        return dp[m][n];
    }
}