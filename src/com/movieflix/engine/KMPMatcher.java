package com.movieflix.engine;

public class KMPMatcher {
    public static boolean search(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        int[] lps = computeLPS(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++; j++;
            }
            if (j == pattern.length()) return true;
            else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) j = lps[j - 1];
                else i++;
            }
        }
        return false;
    }

    private static int[] computeLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0, i = 1;
        lps[0] = 0;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++; lps[i] = len; i++;
            } else {
                if (len != 0) len = lps[len - 1];
                else { lps[i] = 0; i++; }
            }
        }
        return lps;
    }
}