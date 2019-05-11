package work;

import com.oocourse.specs2.models.Graph;
import com.oocourse.specs2.models.NodeIdNotFoundException;
import com.oocourse.specs2.models.NodeNotConnectedException;
import com.oocourse.specs2.models.Path;
import com.oocourse.specs2.models.PathIdNotFoundException;
import com.oocourse.specs2.models.PathNotFoundException;

import java.util.HashMap;
import java.util.Iterator;

public class MyGraph implements Graph {

    private final HashMap<Path, Integer> pathIds;
    private final HashMap<Integer, Path> paths;
    private int id;
    private final HashMap<Integer, Integer> cnt;
    private final UndirectedGraph graph;
    private boolean changed;

    public MyGraph() {
        pathIds = new HashMap<>();
        paths = new HashMap<>();
        cnt = new HashMap<>();
        id = 1;
        graph = new UndirectedGraph();
        changed = true;
    }

    @Override
    public boolean containsNode(int nodeId) {
        return cnt.containsKey(nodeId);
    }

    @Override
    public boolean containsEdge(int fromNodeId, int toNodeId) {
        if (!containsNode(fromNodeId) || !containsNode(toNodeId)) {
            return false;
        }
        return graph.containsEdge(fromNodeId, toNodeId);
    }

    @Override
    public boolean isConnected(int fromNodeId, int toNodeId)
            throws NodeIdNotFoundException {
        if (!containsNode(fromNodeId)) {
            throw new NodeIdNotFoundException(fromNodeId);
        }
        if (!containsNode(toNodeId)) {
            throw new NodeIdNotFoundException(toNodeId);
        }
        if (changed) {
            graph.recalculate();
            changed = false;
        }
        return graph.shortestPathLength(fromNodeId, toNodeId) < 10000;
    }

    @Override
    public int getShortestPathLength(int fromNodeId, int toNodeId)
            throws NodeIdNotFoundException, NodeNotConnectedException {
        if (!isConnected(fromNodeId, toNodeId)) {
            throw new NodeNotConnectedException(fromNodeId, toNodeId);
        }
        return graph.shortestPathLength(fromNodeId, toNodeId);
    }

    @Override
    public int size() {
        return pathIds.size();
    }

    @Override
    public boolean containsPath(Path path) {
        if (path == null) {
            return false;
        }
        return pathIds.containsKey(path);
    }

    @Override
    public boolean containsPathId(int pathId) {
        return paths.containsKey(pathId);
    }

    @Override
    public Path getPathById(int pathId)
            throws PathIdNotFoundException {
        if (!containsPathId(pathId)) {
            throw new PathIdNotFoundException(pathId);
        }
        return paths.get(pathId);
    }

    @Override
    public int getPathId(Path path)
            throws PathNotFoundException {
        if (path == null || !path.isValid() || !containsPath(path)) {
            throw new PathNotFoundException(path);
        }
        return pathIds.get(path);
    }

    @Override
    public int addPath(Path path) {
        if (!(path instanceof MyPath) || !path.isValid()) {
            return 0;
        }
        if (pathIds.containsKey(path)) {
            return pathIds.get(path);
        }
        pathIds.put(path, id);
        paths.put(id, path);
        for (int e : ((MyPath) path).getNodes()) {
            if (cnt.containsKey(e)) {
                cnt.put(e, cnt.get(e) + 1);
            } else {
                cnt.put(e, 1);
            }
        }
        changed = true;
        Iterator<Integer> iterator = path.iterator();
        int v1 = iterator.next();
        int v2;
        while (iterator.hasNext()) {
            v2 = iterator.next();
            graph.addEdge(v1, v2, 1);
            v1 = v2;
        }
        return id++;
    }

    @Override
    public int removePath(Path path)
            throws PathNotFoundException {
        if (!(path instanceof MyPath)
                || !path.isValid() || !containsPath(path)) {
            throw new PathNotFoundException(path);
        }
        int pathId = pathIds.get(path);
        paths.remove(pathId);
        pathIds.remove(path);
        for (int e : ((MyPath) path).getNodes()) {
            int tmp = cnt.get(e) - 1;
            if (tmp <= 0) {
                cnt.remove(e);
            } else {
                cnt.put(e, tmp);
            }
        }
        changed = true;
        Iterator<Integer> iterator = path.iterator();
        int v1 = iterator.next();
        int v2;
        while (iterator.hasNext()) {
            v2 = iterator.next();
            graph.removeEdge(v1, v2);
            v1 = v2;
        }
        return pathId;
    }

    @Override
    public void removePathById(int pathId)
            throws PathIdNotFoundException {
        if (!paths.containsKey(pathId)) {
            throw new PathIdNotFoundException(pathId);
        }
        try {
            removePath(paths.get(pathId));
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDistinctNodeCount() {
        return cnt.size();
    }

}
