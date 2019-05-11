package work;

import java.util.HashMap;
import java.util.LinkedList;

public class UndirectedGraph {

    private class Pair {
        private int num;
        private int length;

        Pair(int t, int l) {
            num = t;
            length = l;
        }
    }

    private HashMap<Integer, HashMap<Integer, Pair>> edgeAdj = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Integer>>
            shortest = new HashMap<>();

    private void putMap(int v1, int v2, int len) {
        if (edgeAdj.containsKey(v1)) {
            HashMap<Integer, Pair> tmp = edgeAdj.get(v1);
            if (tmp.containsKey(v2)) {
                tmp.get(v2).num++;
            } else {
                tmp.put(v2, new Pair(1, len));
            }
        } else {
            edgeAdj.put(v1, new HashMap<>());
            edgeAdj.get(v1).put(v2, new Pair(1, len));
        }
    }

    public void addEdge(int v1, int v2, int len) {
        putMap(v1, v2, len);
        putMap(v2, v1, len);
    }

    public boolean containsEdge(int v1, int v2) {
        return edgeAdj.containsKey(v1) &&
                edgeAdj.get(v1).containsKey(v2);
    }

    private void removeMap(int v1, int v2) {
        if (!containsEdge(v1, v2)) {
            return;
        }
        HashMap<Integer, Pair> map = edgeAdj.get(v1);
        int times = --map.get(v2).num;
        if (times <= 0) {
            map.remove(v2);
            if (map.size() <= 0) {
                edgeAdj.remove(v1);
            }
        }
    }

    public void removeEdge(int v1, int v2) {
        removeMap(v1, v2);
        removeMap(v2, v1);
    }

    /*private void dijkstra(int src) {
    }*/

    private void bfs(int src) {
        LinkedList<Pair> list = new LinkedList<>();
        list.addLast(new Pair(src, 0));
        shortest.put(src, new HashMap<>());
        shortest.get(src).put(src, 0);
        while (!list.isEmpty()) {
            Pair pair = list.removeFirst();
            HashMap<Integer, Pair> map = edgeAdj.get(pair.num);
            for (int e : map.keySet()) {
                if (!shortest.get(src).containsKey(e)) {
                    shortest.get(src).put(e, pair.length + 1);
                    list.addLast(new Pair(e, pair.length + 1));
                }
            }
        }
    }

    public void recalculate() {
        shortest.clear();
        for (int p : edgeAdj.keySet()) {
            bfs(p);
        }
    }

    public int shortestPathLength(int v1, int v2) {
        if (shortest.containsKey(v1) &&
                shortest.get(v1).containsKey(v2)) {
            return shortest.get(v1).get(v2);
        }
        return 20000;
    }

}
