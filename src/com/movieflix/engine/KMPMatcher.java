package com.movieflix.engine;

public class KMPMatcher {

    public static boolean search(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) return true;
        if (text == null || text.isEmpty()) return false;

        // Normalize to lowercase for case-insensitive pattern matching
        String t = text.toLowerCase();
        String p = pattern.toLowerCase();

        int n = t.length();
        int m = p.length();
        int[] lps = computeLPS(p);

        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            if (p.charAt(j) == t.charAt(i)) {
                i++;
                j++;
            }
            if (j == m) {
                return true; // Match found
            } else if (i < n && p.charAt(j) != t.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    private static int[] computeLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }
}