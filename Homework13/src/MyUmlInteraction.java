import com.oocourse.uml1.interact.common.AttributeClassInformation;
import com.oocourse.uml1.interact.common.AttributeQueryType;
import com.oocourse.uml1.interact.common.OperationQueryType;
import com.oocourse.uml1.interact.exceptions.user.AttributeDuplicatedException;
import com.oocourse.uml1.interact.exceptions.user.AttributeNotFoundException;
import com.oocourse.uml1.interact.exceptions.user.ClassDuplicatedException;
import com.oocourse.uml1.interact.exceptions.user.ClassNotFoundException;
import com.oocourse.uml1.interact.format.UmlInteraction;
import com.oocourse.uml1.models.common.Visibility;
import com.oocourse.uml1.models.elements.UmlAssociation;
import com.oocourse.uml1.models.elements.UmlAssociationEnd;
import com.oocourse.uml1.models.elements.UmlAttribute;
import com.oocourse.uml1.models.elements.UmlElement;
import com.oocourse.uml1.models.elements.UmlGeneralization;
import com.oocourse.uml1.models.elements.UmlInterfaceRealization;
import com.oocourse.uml1.models.elements.UmlOperation;
import com.oocourse.uml1.models.elements.UmlParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyUmlInteraction implements UmlInteraction {

    private final HashMap<String, HashSet<String>> classSet = new HashMap<>();
    private final HashMap<String, ClassInfo> classMap = new HashMap<>();
    private final HashMap<String, HashSet<String>> interfaces
            = new HashMap<>(); // extends
    private final HashMap<String, String> interfaceMap = new HashMap<>();
    private final HashMap<String, String> endMap = new HashMap<>();
    private final HashMap<String, String> optMap = new HashMap<>();
    private int classCount = 0;

    public MyUmlInteraction(UmlElement... elements) {
        HashMap<String, UmlElement> map = new HashMap<>();
        for (UmlElement element : elements) {
            switch (element.getElementType()) {
                case UML_CLASS:
                    addClass(element);
                    break;
                case UML_INTERFACE:
                    addInterface(element);
                    break;
                case UML_ASSOCIATION_END:
                    addAssociationEnd(element);
                    break;
                case UML_OPERATION:
                    optMap.put(element.getId(), element.getParentId());
                    map.put(element.getId(), element);
                    break;
                default:
            }
        }
        for (String optId : optMap.keySet()) {
            addOpt(map, optId);
        }
        for (String endId : endMap.keySet()) {
            String classId = endMap.get(endId);
            if (classMap.containsKey(classId)) {
                classMap.get(classId).ass();
            }
        }
        for (UmlElement e : elements) {
            switch (e.getElementType()) {
                case UML_ASSOCIATION:
                    addAss((UmlAssociation) e);
                    break;
                case UML_ATTRIBUTE:
                    addAttribute(e);
                    break;
                case UML_PARAMETER:
                    addParam((UmlParameter) e);
                    break;
                case UML_GENERALIZATION:
                    addGen((UmlGeneralization) e);
                    break;
                case UML_INTERFACE_REALIZATION:
                    addRealization((UmlInterfaceRealization) e);
                    break;
                default:
            }
        }
    }

    private void addRealization(UmlInterfaceRealization realization) {
        String classId = realization.getSource();
        String interfaceId = realization.getTarget();
        classMap.get(classId).addInterface(interfaceId,
                interfaceMap.get(interfaceId));
    }

    private void addGen(UmlGeneralization generalization) {
        String source = generalization.getSource();
        String target = generalization.getTarget();
        if (classMap.containsKey(source)) {
            classMap.get(source).setSuperClassId(target);
        } else {
            interfaces.get(source).add(target);
        }
    }

    private void addAss(UmlAssociation association) {
        String id1 = endMap.get(association.getEnd1());
        String id2 = endMap.get(association.getEnd2());
        if (classMap.containsKey(id1)) {
            classMap.get(id1).addAssociation(id2, classMap.containsKey(id2));
        }
        if (classMap.containsKey(id2)) {
            classMap.get(id2).addAssociation(id1, classMap.containsKey(id1));
        }
    }

    private void addOpt(HashMap<String, UmlElement> map, String optId) {
        String classId = optMap.get(optId);
        if (classMap.containsKey(classId)) {
            classMap.get(classId).
                    addOperation((UmlOperation) map.get(optId));
        }
    }

    private void addParam(UmlParameter e) {
        String id = optMap.get(e.getParentId());
        if (classMap.containsKey(id)) {
            classMap.get(id).addPara(e);
        }
    }

    private void addAttribute(UmlElement e) {
        if (classMap.containsKey(e.getParentId())) {
            ClassInfo info = classMap.get(e.getParentId());
            info.addAttr((UmlAttribute) e, info.getClassName());
        }
    }

    private void addAssociationEnd(UmlElement element) {
        UmlAssociationEnd end = (UmlAssociationEnd) element;
        endMap.put(element.getId(), end.getReference());
    }

    private void addInterface(UmlElement element) {
        interfaces.put(element.getId(), new HashSet<>());
        interfaceMap.put(element.getId(), element.getName());
    }

    private void addClass(UmlElement element) {
        classCount++;
        String name = element.getName();
        if (!classSet.containsKey(name)) {
            classSet.put(name, new HashSet<>());
        }
        classSet.get(name).add(element.getId());
        classMap.put(element.getId(), new ClassInfo(name));
    }

    private void checkException(String className)
            throws ClassNotFoundException, ClassDuplicatedException {
        if (!classSet.containsKey(className)) {
            throw new ClassNotFoundException(className);
        }
        if (classSet.get(className).size() > 1) {
            throw new ClassDuplicatedException(className);
        }
    }

    @Override
    public int getClassCount() {
        return classCount;
    }

    @Override
    public int getClassOperationCount(String className,
                                      OperationQueryType queryType)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        ClassInfo info = classMap.get(
                classSet.get(className).iterator().next());
        return info.getOptCnt(queryType);
    }

    private void addSuperAttr(String classId) {
        ClassInfo info = classMap.get(classId);
        if (info.isSuperAttrIncluded()) {
            return;
        }
        info.superHasAttrIncluded();
        String fatherId = info.getSuperClassId();
        if (fatherId != null) {
            addSuperAttr(fatherId);
            HashMap<String, LinkedList<UmlAttribute>> attributes =
                    classMap.get(fatherId).getAttributes();
            for (String name : attributes.keySet()) {
                for (UmlAttribute attribute : attributes.get(name)) {
                    info.addAttr(attribute,
                            classMap.get(attribute.getParentId()).
                                    getClassName());
                }
            }
        }
    }

    private void addSuperAss(String classId) {
        ClassInfo info = classMap.get(classId);
        if (info.isSuperAssIncluded()) {
            return;
        }
        info.superHasAssIncluded();
        String fatherId = info.getSuperClassId();
        if (fatherId != null) {
            addSuperAss(fatherId);
            HashSet<String> associations =
                    classMap.get(fatherId).getAssSet();
            for (String id : associations) {
                info.addAssociation(id, true);
            }
            info.setNumOfAss(info.getNumOfAss() +
                    classMap.get(fatherId).getNumOfAss());
        }
    }

    @Override
    public int getClassAttributeCount(String className,
                                      AttributeQueryType queryType)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        addSuperAttr(id);
        switch (queryType) {
            case ALL:
                return classMap.get(id).getNumOfAttr();
            case SELF_ONLY:
                return classMap.get(id).getNumOfSelfAttr();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public int getClassAssociationCount(String className)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        addSuperAss(id);
        return classMap.get(id).getNumOfAss();
    }

    @Override
    public List<String> getClassAssociatedClassList(String className)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        ClassInfo info = classMap.get(id);
        if (info.getAssList() == null) {
            addSuperAss(id);
            LinkedList<String> list = new LinkedList<>();
            for (String classId : info.getAssSet()) {
                list.addLast(classMap.get(classId).getClassName());
            }
            info.setAssList(list);
        }
        return info.getAssList();
    }

    @Override
    public Map<Visibility, Integer>
        getClassOperationVisibility(String className, String operationName)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        HashMap<Visibility, Integer> map = new HashMap<>();
        map.put(Visibility.PUBLIC, 0);
        map.put(Visibility.PACKAGE, 0);
        map.put(Visibility.DEFAULT, 0);
        map.put(Visibility.PROTECTED, 0);
        map.put(Visibility.PRIVATE, 0);
        ClassInfo info = classMap.get(
                classSet.get(className).iterator().next());
        if (info.getOperations().containsKey(operationName)) {
            for (UmlOperation operation :
                    info.getOperations().get(operationName)) {
                Visibility visibility = operation.getVisibility();
                map.put(visibility, map.get(visibility) + 1);
            }
        }
        return map;
    }

    @Override
    public Visibility getClassAttributeVisibility(String className,
                                                  String attributeName)
            throws ClassNotFoundException, ClassDuplicatedException,
            AttributeNotFoundException, AttributeDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        addSuperAttr(id);
        return classMap.get(id).getAttributeVisibility(attributeName);
    }

    private String findTopClass(String id) {
        String parentId = classMap.get(id).getSuperClassId();
        if (parentId == null) {
            return id;
        } else if (classMap.get(id).getTopClassId() == null) {
            String topId = findTopClass(parentId);
            classMap.get(id).setTopClassId(topId);
            classMap.get(id).setTopClassName(
                    classMap.get(topId).getClassName());
        }
        return classMap.get(id).getTopClassId();
    }

    @Override
    public String getTopParentClass(String className)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        if (classMap.get(id).getSuperClassId() == null) {
            return className;
        }
        findTopClass(id);
        return classMap.get(id).getTopClassName();
    }

    private void addInterfaceGroup(ClassInfo info) {
        if (info.isInterfacesCompleted()) {
            return;
        }
        info.hasInterfacesCompleted();
        LinkedList<String> list = info.getInterfaceIds();
        ArrayList<String> arrayList = new ArrayList<>(list);
        for (int i = 0; i < arrayList.size(); i++) {
            String iid = arrayList.get(i);
            for (String sid : interfaces.get(iid)) {
                if (info.addInterface(sid, interfaceMap.get(sid))) {
                    arrayList.add(sid);
                }
            }
        }
    }

    private void addSuperInterface(String id) {
        ClassInfo info = classMap.get(id);
        String parentId = info.getSuperClassId();
        if (parentId != null) {
            addSuperInterface(parentId);
            HashMap<String, String> map =
                    classMap.get(parentId).getInterfaceMap();
            for (String interfaceId : map.keySet()) {
                info.addInterface(interfaceId, map.get(interfaceId));
            }
        }
    }

    @Override
    public List<String> getImplementInterfaceList(String className)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        ClassInfo info = classMap.get(id);
        if (!info.isSuperInterfaceIncluded()) {
            info.superHasInterfaceIncluded();
            addSuperInterface(id);
        }
        addInterfaceGroup(info);
        return info.getInterfaces();
    }

    @Override
    public List<AttributeClassInformation>
        getInformationNotHidden(String className)
            throws ClassNotFoundException, ClassDuplicatedException {
        checkException(className);
        String id = classSet.get(className).iterator().next();
        addSuperAttr(id);
        return classMap.get(id).getNotHiddenList();
    }
}
