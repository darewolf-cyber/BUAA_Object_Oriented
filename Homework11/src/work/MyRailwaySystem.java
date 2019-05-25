package work;

import com.oocourse.specs3.models.NodeIdNotFoundException;
import com.oocourse.specs3.models.NodeNotConnectedException;
import com.oocourse.specs3.models.Path;
import com.oocourse.specs3.models.PathIdNotFoundException;
import com.oocourse.specs3.models.PathNotFoundException;
import com.oocourse.specs3.models.RailwaySystem;
import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

public class MyRailwaySystem extends MyGraph implements RailwaySystem {

    private static int unpleasantValue(int u, int v) {
        int v1 = ((u % 5) + 5) % 5 * 2;
        int v2 = ((v % 5) + 5) % 5 * 2;
        if (v1 > v2) {
            return 1 << v1;
        } else {
            return 1 << v2;
        }
    }

    private static long mixId(int nodeId, int pathId) {
        long tmp = ((long) nodeId) & 0x00000000_ffffffffL;
        return tmp + (((long) pathId) << 32);
    }

    private static int mixId2NodeId(long mix) {
        return (int) (mix & 0x00000000_ffffffffL);
    }

    private class Edge {
        private int transfer;
        private int price;
        private int unpleasant;

        Edge(int t, int p, int u) {
            transfer = t;
            price = p;
            unpleasant = u;
        }
    }

    private boolean removed;
    private final HashMap<Integer, Integer> ufs;
    private final HashMap<Long, HashMap<Long, Edge>> adj;
    private HashMap<Long, Edge> map;
    private final HashMap<Integer, HashMap<Integer, Integer>> price;
    private final HashMap<Integer, HashMap<Integer, Integer>> transfer;
    private final HashMap<Integer, HashMap<Integer, Integer>> unpleasantValue;
    private final HashMap<Integer, HashSet<Integer>> pointEdge;

    public MyRailwaySystem() {
        super();
        ufs = new HashMap<>();
        removed = false;
        adj = new HashMap<>();
        price = new HashMap<>();
        transfer = new HashMap<>();
        unpleasantValue = new HashMap<>();
        pointEdge = new HashMap<>();
    }

    private int getRoot(int x) {
        int r = x;
        while (ufs.containsKey(r)) {
            r = ufs.get(r);
        }
        return r;
    }

    private void addNode(int x, int father) {
        int r1 = getRoot(x);
        int r2 = getRoot(father);
        if (r1 != r2) {
            ufs.put(r1, r2);
        }
    }

    private class Length implements Comparable<Length> {
        private int len;
        private long toNode;

        Length(int l, long t) {
            len = l;
            toNode = t;
        }

        @Override
        public int compareTo(@NotNull Length o) {
            return Integer.compare(len, o.len);
        }
    }

    @SuppressWarnings("unchecked")
    private void dijkstra(long src, int choose) {
        final HashMap[] arr = {price, transfer, unpleasantValue};
        PriorityQueue<Length> queue = new PriorityQueue<>();
        HashMap<Long, Integer> dis = new HashMap<>();
        HashSet<Long> vis = new HashSet<>();
        dis.put(src, 0);
        vis.add(src);
        long s = src;
        int cnt = 1;
        while (cnt < adj.size()) {
            map = adj.get(s);
            for (long key : map.keySet()) {
                int d = dis.get(s);
                switch (choose) {
                    case 1:
                        d += map.get(key).price;
                        break;
                    case 2:
                        d += map.get(key).transfer;
                        break;
                    case 3:
                        d += map.get(key).unpleasant;
                        break;
                    default:
                }
                if (!dis.containsKey(key) || d < dis.get(key)) {
                    dis.put(key, d);
                    queue.add(new Length(d, key));
                }
            }
            Length lll;
            if (queue.isEmpty()) {
                return;
            }
            do {
                lll = queue.remove();
                s = lll.toNode;
            } while (vis.contains(s) && !queue.isEmpty());
            vis.add(s);
            HashMap<Integer, HashMap<Integer, Integer>> tmp = arr[choose - 1];
            int u = mixId2NodeId(src);
            int v = mixId2NodeId(s);
            if (!tmp.containsKey(u) || !tmp.get(u).containsKey(v) ||
                    tmp.get(u).get(v) > lll.len) {
                if (!tmp.containsKey(u)) {
                    tmp.put(u, new HashMap<>());
                }
                tmp.get(u).put(v, lll.len);
            }
            cnt++;
        }
    }

    @Override
    public int getLeastTicketPrice(int fromNodeId, int toNodeId)
            throws NodeIdNotFoundException, NodeNotConnectedException {
        if (!isConnected(fromNodeId, toNodeId)) {
            throw new NodeNotConnectedException(fromNodeId, toNodeId);
        }
        if (fromNodeId == toNodeId) {
            return 0;
        }
        if (price.containsKey(fromNodeId)) {
            return price.get(fromNodeId).get(toNodeId);
        } else if (price.containsKey(toNodeId)) {
            return price.get(toNodeId).get(fromNodeId);
        } else {
            for (int pid : pointEdge.get(fromNodeId)) {
                dijkstra(mixId(fromNodeId, pid), 1);
            }
            return price.get(fromNodeId).get(toNodeId);
        }
    }

    @Override
    public int getLeastTransferCount(int fromNodeId, int toNodeId)
            throws NodeIdNotFoundException, NodeNotConnectedException {
        if (!isConnected(fromNodeId, toNodeId)) {
            throw new NodeNotConnectedException(fromNodeId, toNodeId);
        }
        if (fromNodeId == toNodeId) {
            return 0;
        }
        if (transfer.containsKey(fromNodeId)) {
            return transfer.get(fromNodeId).get(toNodeId);
        } else if (transfer.containsKey(toNodeId)) {
            return transfer.get(toNodeId).get(fromNodeId);
        } else {
            for (int pid : pointEdge.get(fromNodeId)) {
                dijkstra(mixId(fromNodeId, pid), 2);
            }
            return transfer.get(fromNodeId).get(toNodeId);
        }
    }

    @Override
    public int getLeastUnpleasantValue(int fromNodeId, int toNodeId)
            throws NodeIdNotFoundException, NodeNotConnectedException {
        if (!isConnected(fromNodeId, toNodeId)) {
            throw new NodeNotConnectedException(fromNodeId, toNodeId);
        }
        if (fromNodeId == toNodeId) {
            return 0;
        }
        if (unpleasantValue.containsKey(fromNodeId)) {
            return unpleasantValue.get(fromNodeId).get(toNodeId);
        } else if (unpleasantValue.containsKey(toNodeId)) {
            return unpleasantValue.get(toNodeId).get(fromNodeId);
        } else {
            for (int pid : pointEdge.get(fromNodeId)) {
                dijkstra(mixId(fromNodeId, pid), 3);
            }
            return unpleasantValue.get(fromNodeId).get(toNodeId);
        }
    }

    @Override
    public int getConnectedBlockCount() {
        if (removed) {
            ufs.clear(); // 1 (233: 2)
            HashMap<Integer, Path> paths = getPaths();
            for (int key : paths.keySet()) {
                Iterator<Integer> iterator = paths.get(key).iterator();
                int v1 = iterator.next();
                int v2;
                while (iterator.hasNext()) {
                    v2 = iterator.next();
                    addNode(v2, v1); // 3 (317: 4)
                    v1 = v2;
                }
            }
            removed = false;
        }
        return getDistinctNodeCount() - ufs.size();
    }

    private void addEdge(int v1, int v2, int pathId) {
        if (v1 == v2) {
            return;
        }
        if (!removed) {
            addNode(v1, v2);
        }
        long vs = mixId(v1, 10000);
        long vf = mixId(v1, 20000);
        if (!adj.containsKey(vs)) {
            adj.put(vs, new HashMap<>());
            adj.put(vf, new HashMap<>());
            adj.get(vf).put(vs, new Edge(1, 2, 32));
        }
        long p1 = mixId(v1, pathId); // v1's real id
        adj.get(vs).put(p1, new Edge(0, 0, 0));
        if (!adj.containsKey(p1)) {
            adj.put(p1, new HashMap<>());
        }
        map = adj.get(p1);
        map.put(vf, new Edge(0, 0, 0));
        map.put(mixId(v2, pathId), new Edge(0, 1, unpleasantValue(v1, v2)));
        if (!pointEdge.containsKey(v1)) {
            pointEdge.put(v1, new HashSet<>());
        }
        pointEdge.get(v1).add(pathId);
    }

    private void removeEdge(int v1, int v2, int pathId) {
        if (v1 == v2) {
            return;
        }
        long p1 = mixId(v1, pathId);
        long p2 = mixId(v2, pathId);
        if (!adj.containsKey(p1)) {
            return;
        }
        map = adj.get(p1);
        if (!map.containsKey(p2)) {
            return;
        }
        map.remove(p2);
        if (pointEdge.containsKey(v1)) {
            pointEdge.get(v1).remove(pathId);
            long vs = mixId(v1, 10000);
            long vf = mixId(v1, 20000);
            adj.get(vs).remove(p1);
            map.remove(vf);
            if (pointEdge.get(v1).size() == 0) {
                pointEdge.remove(v1);
                adj.remove(vs);
                adj.remove(vf);
            }
        }
    }

    @Override
    public int addPath(Path path) {
        int id = getId();
        int pathId = super.addPath(path);
        if (pathId != id) {
            return pathId;
        }
        Iterator<Integer> iterator = path.iterator();
        int v1 = iterator.next();
        int v2;
        while (iterator.hasNext()) {
            v2 = iterator.next();
            addEdge(v1, v2, pathId);
            addEdge(v2, v1, pathId);
            v1 = v2;
        }
        price.clear();
        transfer.clear();
        unpleasantValue.clear();
        return pathId;
    }

    @Override
    public int removePath(Path path)
            throws PathNotFoundException {
        removed = true;
        int pathId = super.removePath(path);
        Iterator<Integer> iterator = path.iterator();
        int v1 = iterator.next();
        int v2;
        while (iterator.hasNext()) {
            v2 = iterator.next();
            removeEdge(v1, v2, pathId);
            removeEdge(v2, v1, pathId);
            v1 = v2;
        }
        price.clear();
        transfer.clear();
        unpleasantValue.clear();
        return pathId;
    }

    @Override
    public void removePathById(int pathId)
            throws PathIdNotFoundException {
        try {
            removePath(getPathById(pathId));
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        }
    }

    // unused
    public boolean containsPathSequence(Path[] pseq) {
        return false;
    }

    public boolean isConnectedInPathSequence(Path[] pseq,
                                             int fromNodeId, int toNodeId) {
        return false;
    }

    public int getTicketPrice(Path[] pseq, int fromNodeId, int toNodeId) {
        return 0;
    }

    public int getUnpleasantValue(Path path, int[] idx) {
        return 0;
    }

    public int getUnpleasantValue(Path path, int fromIndex, int toIndex) {
        return 0;
    }

    public int getUnpleasantValue(Path[] pseq, int fromNodeId, int toNodeId) {
        return 0;
    }
}
