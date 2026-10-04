package com.movieflix.engine;

public class RabinKarp {
    private static final int PRIME = 101;

    public static boolean search(String text, String pattern) {
        int m = pattern.length(), n = text.length();
        if (m > n) return false;
        long patternHash = 0, textHash = 0, h = 1;
        for (int i = 0; i < m - 1; i++) h = (h * 256) % PRIME;
        for (int i = 0; i < m; i++) {
            patternHash = (256 * patternHash + pattern.charAt(i)) % PRIME;
            textHash = (256 * textHash + text.charAt(i)) % PRIME;
        }
        for (int i = 0; i <= n - m; i++) {
            if (patternHash == textHash) {
                if (text.substring(i, i + m).equals(pattern)) return true;
            }
            if (i < n - m) {
                textHash = (256 * (textHash - text.charAt(i) * h) + text.charAt(i + m)) % PRIME;
                if (textHash < 0) textHash += PRIME;
            }
        }
        return false;
    }
}