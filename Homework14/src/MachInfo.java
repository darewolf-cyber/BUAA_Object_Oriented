import com.oocourse.uml2.interact.exceptions.user.StateDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.StateNotFoundException;
import com.oocourse.uml2.models.elements.UmlElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MachInfo {

    private final String machName;
    private int transNum = 0;

    private class StateInfo {
        private HashSet<String> nextIds = new HashSet<>();
        private ArrayList<String> idArray = new ArrayList<>();
        private boolean subAdded = false;
    }

    private final HashMap<String, LinkedList<String>> states = new HashMap<>();
    private final HashMap<String, StateInfo> stateMap = new HashMap<>();

    MachInfo(String machName) {
        this.machName = machName;
    }

    public int getStateNum() {
        return stateMap.size();
    }

    public int getTransNum() {
        return transNum;
    }

    void addState(UmlElement state) {
        String id = state.getId();
        String name = state.getName();
        if (name == null) {
            name = "";
        }
        if (!states.containsKey(name)) {
            states.put(name, new LinkedList<>());
        }
        states.get(name).addLast(id);
        if (!stateMap.containsKey(id)) {
            stateMap.put(id, new StateInfo());
        }
    }

    void addTrans(String source, String target, boolean creating) {
        if (creating) {
            transNum++;
        }
        if (!stateMap.containsKey(source)) {
            stateMap.put(source, new StateInfo());
        }
        if (stateMap.get(source).nextIds.add(target)) {
            stateMap.get(source).idArray.add(target);
        }
    }

    private void subBfs(String sourceId) {
        StateInfo info = stateMap.get(sourceId);
        if (info.subAdded) {
            return;
        }
        for (int i = 0; i < info.idArray.size(); i++) {
            for (String id : stateMap.get(info.idArray.get(i)).idArray) {
                addTrans(sourceId, id, false);
            }
        }
        info.subAdded = true;
    }

    int getSubNum(String source)
            throws StateNotFoundException, StateDuplicatedException {
        if (!states.containsKey(source)) {
            throw new StateNotFoundException(machName, source);
        } else if (states.get(source).size() > 1) {
            throw new StateDuplicatedException(machName, source);
        }
        String id = states.get(source).getFirst();
        subBfs(id);
        return stateMap.get(id).idArray.size();
    }
}
