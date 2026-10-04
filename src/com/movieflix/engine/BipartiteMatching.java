package com.movieflix.engine;
import java.util.*;

public class BipartiteMatching {
    public static boolean bpm(int[][] bpGraph, int u, boolean[] seen, int[] matchR) {
        int n = bpGraph[0].length;
        for (int v = 0; v < n; v++) {
            if (bpGraph[u][v] == 1 && !seen[v]) {
                seen[v] = true;
                if (matchR[v] < 0 || bpm(bpGraph, matchR[v], seen, matchR)) {
                    matchR[v] = u;
                    return true;
                }
            }
        }
        return false;
    }

    public static int maxBPM(int[][] bpGraph) {
        int m = bpGraph.length, n = bpGraph[0].length;
        int[] matchR = new int[n];
        Arrays.fill(matchR, -1);
        int count = 0;
        for (int i = 0; i < m; i++) {
            boolean[] seen = new boolean[n];
            if (bpm(bpGraph, i, seen, matchR)) count++;
        }
        return count;
    }
}