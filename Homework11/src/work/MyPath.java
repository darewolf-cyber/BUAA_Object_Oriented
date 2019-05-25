package work;

import com.oocourse.specs3.models.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class MyPath implements Path
{
    private final HashSet<Integer> nodes;
    private final ArrayList<Integer> list;

    public MyPath(int... nodeList) {
        nodes = new HashSet<>();
        list = new ArrayList<>(nodeList.length);
        for (int e : nodeList) {
            nodes.add(e);
            list.add(e);
        }
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public int getNode(int index) {
        return list.get(index - 1);
    }

    @Override
    public boolean containsNode(int node) {
        return nodes.contains(node);
    }

    @Override
    public int getDistinctNodeCount() {
        return nodes.size();
    }

    @Override
    public boolean isValid() {
        return list.size() >= 2;
    }

    @Override
    public int getUnpleasantValue(int nodeId) {
        return 0;
    }

    @Override
    public int compareTo(Path o) {
        final boolean flag = size() < o.size();
        final int l;
        if (flag) {
            l = size();
        } else {
            l = o.size();
        }
        for (int i = 0; i < l; i++) {
            if (getNode(i + 1) < o.getNode(i + 1)) {
                return -1;
            } else if (getNode(i + 1) > o.getNode(i + 1)) {
                return 1;
            }
        }
        if (size() == o.size()) {
            return 0;
        } else if (flag) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MyPath) {
            MyPath path = (MyPath) obj;
            if (size() != path.size()) {
                return false;
            }
            final int l = size();
            for (int i = 0; i < l; i++) {
                if (getNode(i + 1) != path.getNode(i + 1)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        // TODO:
        return list.iterator();
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    HashSet<Integer> getNodes() {
        return nodes;
    }

    public boolean containsEdge(int fromNodeId, int toNodeId) {
        return false;
    }

    public int getShortestPathLength(int fromNodeId, int toNodeId) {
        return 0;
    }
}
