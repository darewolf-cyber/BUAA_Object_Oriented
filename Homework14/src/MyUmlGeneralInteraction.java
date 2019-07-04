import com.oocourse.uml2.interact.common.AttributeClassInformation;
import com.oocourse.uml2.interact.exceptions.user.InteractionDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.InteractionNotFoundException;
import com.oocourse.uml2.interact.exceptions.user.LifelineDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.LifelineNotFoundException;
import com.oocourse.uml2.interact.exceptions.user.StateDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.StateMachineDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.StateMachineNotFoundException;
import com.oocourse.uml2.interact.exceptions.user.StateNotFoundException;
import com.oocourse.uml2.interact.exceptions.user.UmlRule002Exception;
import com.oocourse.uml2.interact.exceptions.user.UmlRule008Exception;
import com.oocourse.uml2.interact.exceptions.user.UmlRule009Exception;
import com.oocourse.uml2.interact.format.UmlGeneralInteraction;
import com.oocourse.uml2.models.elements.UmlAttribute;
import com.oocourse.uml2.models.elements.UmlClassOrInterface;
import com.oocourse.uml2.models.elements.UmlElement;
import com.oocourse.uml2.models.elements.UmlLifeline;
import com.oocourse.uml2.models.elements.UmlMessage;
import com.oocourse.uml2.models.elements.UmlTransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class MyUmlGeneralInteraction
        extends MyUmlClassInteraction
        implements UmlGeneralInteraction {

    private final HashMap<String, LinkedList<String>>
            machs = new HashMap<>();
    private final HashMap<String, MachInfo> machMap = new HashMap<>();
    private final HashMap<String, LinkedList<String>>
            inters = new HashMap<>();
    private final HashMap<String, SeqInfo> interMap = new HashMap<>();
    private final HashMap<String, String> reg2mach = new HashMap<>();

    public MyUmlGeneralInteraction(UmlElement... elements) {
        super(elements);
        for (UmlElement e : elements) {
            switch (e.getElementType()) {
                case UML_INTERACTION:
                    addInter(e);
                    break;
                case UML_LIFELINE:
                    interMap.get(e.getParentId()).addObj((UmlLifeline) e);
                    break;
                case UML_MESSAGE:
                    interMap.get(e.getParentId()).addMsg((UmlMessage) e);
                    break;
                case UML_STATE_MACHINE:
                    addMach(e);
                    break;
                case UML_REGION:
                    reg2mach.put(e.getId(), e.getParentId());
                    break;
                case UML_STATE:
                case UML_PSEUDOSTATE:
                case UML_FINAL_STATE:
                    machMap.get(reg2mach.get(e.getParentId())).addState(e);
                    break;
                case UML_TRANSITION:
                    addTrans((UmlTransition) e);
                    break;
                default:
            }
        }
    }

    private void addMach(UmlElement machine) {
        tempAdd(machine, machs);
        machMap.put(machine.getId(), new MachInfo(machine.getName()));
    }

    private void addInter(UmlElement element) {
        tempAdd(element, inters);
        interMap.put(element.getId(), new SeqInfo(element.getName()));
    }

    static void tempAdd(UmlElement element,
                 HashMap<String, LinkedList<String>> inters) {
        String name = element.getName();
        if (!inters.containsKey(name)) {
            inters.put(name, new LinkedList<>());
        }
        inters.get(name).addLast(element.getId());
    }

    private void addTrans(UmlTransition transition) {
        String source = transition.getSource();
        String target = transition.getTarget();
        machMap.get(reg2mach.get(transition.getParentId())).
                addTrans(source, target, true);
    }

    // std check
    @Override
    public void checkForUml002() throws UmlRule002Exception {
        HashSet<AttributeClassInformation> result = new HashSet<>();
        HashMap<String, ClassInfo> classMap = getClassMap();
        for (String id : classMap.keySet()) {
            HashMap<String, LinkedList<UmlAttribute>> attributes =
                    classMap.get(id).getAttributes();
            for (String name : attributes.keySet()) {
                if (attributes.get(name).size() > 1 ||
                        classMap.get(id).hasAssName(name)) {
                    result.add(new AttributeClassInformation(name,
                            classMap.get(id).getClassName()));
                }
            }
        }
        if (!result.isEmpty()) {
            throw new UmlRule002Exception(result);
        }
    }

    private HashSet<String> tempSet = new HashSet<>();
    private LinkedList<String> tempStack = new LinkedList<>();

    private void retFromRecur(String id, HashSet<String> result) {
        Iterator<String> iterator = tempStack.iterator();
        String str;
        do {
            str = iterator.next();
            result.add(str);
        } while (iterator.hasNext() && !str.equals(id));
    }

    private void classDfs(String id, HashSet<String> result,
                          HashSet<String> notIds) {
        if (tempSet.contains(id)) {
            retFromRecur(id, result);
            return;
        }
        tempSet.add(id);
        tempStack.addFirst(id);
        if (getClassMap().get(id).getSuperClassId() != null) {
            classDfs(getClassMap().get(id).getSuperClassId(), result, notIds);
        }
        if (!result.contains(id)) {
            notIds.add(id);
        }
        tempSet.remove(id);
        tempStack.removeFirst();
    }

    private void interfaceDfs(String id, HashSet<String> result,
                              HashSet<String> notIds) {
        if (tempSet.contains(id)) {
            retFromRecur(id, result);
            return;
        }
        tempSet.add(id);
        tempStack.addFirst(id);
        for (String str : getInterfaces().get(id)) {
            interfaceDfs(str, result, notIds);
        }
        if (!result.contains(id)) {
            notIds.add(id);
        }
        tempSet.remove(id);
        tempStack.removeFirst();
    }

    @Override
    public void checkForUml008() throws UmlRule008Exception {
        HashSet<UmlClassOrInterface> result = new HashSet<>();
        HashSet<String> ids = new HashSet<>();
        HashSet<String> notIds = new HashSet<>();
        for (String id : getClassMap().keySet()) {
            if (!ids.contains(id) && !notIds.contains(id)) {
                classDfs(id, ids, notIds);
            }
        }
        for (String id : getInterfaces().keySet()) {
            if (!ids.contains(id) && !notIds.contains(id)) {
                interfaceDfs(id, ids, notIds);
            }
        }
        if (!ids.isEmpty()) {
            for (String id : ids) {
                result.add((UmlClassOrInterface) getElementMap().get(id));
            }
            throw new UmlRule008Exception(result);
        }
    }

    @Override
    public void checkForUml009() throws UmlRule009Exception {
        HashSet<UmlClassOrInterface> result = new HashSet<>();
        HashSet<String> ids = new HashSet<>();
        for (String id :getInterfaces().keySet()) {
            interfaceAddSuper(id);
            if (getInterfaceDup().get(id)) {
                ids.add(id);
            }
        }
        for (String id : getClassMap().keySet()) {
            ClassInfo info = getClassMap().get(id);
            if (!info.isSuperInterfaceIncluded()) {
                classAddSuperInterface(id);
            }
            if (info.isDup()) {
                ids.add(id);
            }
        }
        for (String id : getClassMap().keySet()) {
            ClassInfo info = getClassMap().get(id);
            addInterfaceGroup(info);
            if (info.isDup()) {
                ids.add(id);
                continue;
            }
            for (String str : info.getInterfaceIds()) {
                if (getInterfaceDup().get(str)) {
                    ids.add(id);
                    break;
                }
            }
        }
        boolean flag;
        do {
            flag = false;
            for (String id : getClassMap().keySet()) {
                if (ids.contains(getClassMap().get(id).getSuperClassId())) {
                    flag = flag || ids.add(id);
                }
            }
        } while (flag);
        if (!ids.isEmpty()) {
            for (String id : ids) {
                result.add((UmlClassOrInterface) getElementMap().get(id));
            }
            throw new UmlRule009Exception(result);
        }
    }

    // help methods
    private void machException(String name)
            throws StateMachineNotFoundException,
            StateMachineDuplicatedException {
        if (!machs.containsKey(name)) {
            throw new StateMachineNotFoundException(name);
        } else if (machs.get(name).size() > 1) {
            throw new StateMachineDuplicatedException(name);
        }
    }

    private void interException(String name)
            throws InteractionDuplicatedException,
            InteractionNotFoundException {
        if (!inters.containsKey(name)) {
            throw new InteractionNotFoundException(name);
        } else if (inters.get(name).size() > 1) {
            throw new InteractionDuplicatedException(name);
        }
    }

    // state graph
    @Override
    public int getStateCount(String s)
            throws StateMachineNotFoundException,
            StateMachineDuplicatedException {
        machException(s);
        return machMap.get(machs.get(s).getFirst()).getStateNum();
    }

    @Override
    public int getTransitionCount(String s)
            throws StateMachineNotFoundException,
            StateMachineDuplicatedException {
        machException(s);
        return machMap.get(machs.get(s).getFirst()).getTransNum();
    }

    @Override
    public int getSubsequentStateCount(String s, String s1)
            throws StateMachineNotFoundException,
            StateMachineDuplicatedException, StateNotFoundException,
            StateDuplicatedException {
        machException(s);
        return machMap.get(machs.get(s).getFirst()).getSubNum(s1);
    }

    // sequence graph
    @Override
    public int getParticipantCount(String s)
            throws InteractionNotFoundException,
            InteractionDuplicatedException {
        interException(s);
        return interMap.get(inters.get(s).getFirst()).getObjNum();
    }

    @Override
    public int getMessageCount(String s)
            throws InteractionNotFoundException,
            InteractionDuplicatedException {
        interException(s);
        return interMap.get(inters.get(s).getFirst()).getMsgNum();
    }

    @Override
    public int getIncomingMessageCount(String s, String s1)
            throws InteractionNotFoundException, InteractionDuplicatedException,
            LifelineNotFoundException, LifelineDuplicatedException {
        interException(s);
        return interMap.get(inters.get(s).getFirst()).getIncome(s1);
    }

}
