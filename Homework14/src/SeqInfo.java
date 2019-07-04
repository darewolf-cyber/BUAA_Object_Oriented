import com.oocourse.uml2.interact.exceptions.user.LifelineDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.LifelineNotFoundException;
import com.oocourse.uml2.models.elements.UmlLifeline;
import com.oocourse.uml2.models.elements.UmlMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class SeqInfo {

    private final String seqName;
    private int msgNum = 0;

    private final HashMap<String, LinkedList<String>>
            objects = new HashMap<>();
    private final HashMap<String, Integer> objMap = new HashMap<>();
    private final HashSet<String> attrs = new HashSet<>();

    SeqInfo(String seqName) {
        this.seqName = seqName;
    }

    public int getObjNum() {
        return attrs.size();
    }

    public int getMsgNum() {
        return msgNum;
    }

    void addObj(UmlLifeline lifeline) {
        attrs.add(lifeline.getRepresent());
        if (!objMap.containsKey(lifeline.getId())) {
            objMap.put(lifeline.getId(), 0);
        }
        MyUmlGeneralInteraction.tempAdd(lifeline, objects);
    }

    void addMsg(UmlMessage message) {
        msgNum++;
        String target = message.getTarget();
        if (!objMap.containsKey(target)) {
            objMap.put(target, 1);
        } else {
            objMap.put(target, objMap.get(target) + 1);
        }
    }

    public int getIncome(String name)
            throws LifelineNotFoundException, LifelineDuplicatedException {
        if (!objects.containsKey(name)) {
            throw new LifelineNotFoundException(seqName, name);
        } else if (objects.get(name).size() > 1) {
            throw new LifelineDuplicatedException(seqName, name);
        }
        return objMap.get(objects.get(name).getFirst());
    }

}
