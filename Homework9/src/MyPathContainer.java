import com.oocourse.specs1.models.Path;
import com.oocourse.specs1.models.PathContainer;
import com.oocourse.specs1.models.PathIdNotFoundException;
import com.oocourse.specs1.models.PathNotFoundException;

import java.util.HashMap;

public class MyPathContainer implements PathContainer
{
    private final HashMap<Path, Integer> pathIds;
    private final HashMap<Integer, Path> paths;
    private int id;
    private final HashMap<Integer, Integer> cnt;

    public MyPathContainer() {
        pathIds = new HashMap<>();
        paths = new HashMap<>();
        cnt = new HashMap<>();
        id = 1;
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
    public Path getPathById(int pathId) throws PathIdNotFoundException {
        if (!containsPathId(pathId)) {
            throw new PathIdNotFoundException(pathId);
        }
        return paths.get(pathId);
    }

    @Override
    public int getPathId(Path path) throws PathNotFoundException {
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
        return id++;
    }

    @Override
    public int removePath(Path path) throws PathNotFoundException {
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
        return pathId;
    }

    @Override
    public void removePathById(int pathId) throws PathIdNotFoundException {
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
