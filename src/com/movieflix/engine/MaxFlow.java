package com.movieflix.engine;
import java.util.*;

public class MaxFlow {
    public static int computeMaxFlow(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int[][] flow = new int[n][n];
        int[] parent = new int[n];
        int maxFlow = 0;

        while (bfs(capacity, flow, source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v] - flow[u][v]);
            }
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                flow[u][v] += pathFlow;
                flow[v][u] -= pathFlow;
            }
            maxFlow += pathFlow;
        }
        return maxFlow;
    }

    private static boolean bfs(int[][] capacity, int[][] flow, int s, int t, int[] parent) {
        boolean[] visited = new boolean[capacity.length];
        Queue<Integer> q = new LinkedList<>();
        q.add(s); visited[s] = true; parent[s] = -1;

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v = 0; v < capacity.length; v++) {
                if (!visited[v] && capacity[u][v] - flow[u][v] > 0) {
                    q.add(v); parent[v] = u; visited[v] = true;
                    if (v == t) return true;
                }
            }
        }
        return false;
    }
}