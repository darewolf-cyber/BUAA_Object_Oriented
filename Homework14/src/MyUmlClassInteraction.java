import com.oocourse.uml2.interact.common.AttributeClassInformation;
import com.oocourse.uml2.interact.common.AttributeQueryType;
import com.oocourse.uml2.interact.common.OperationQueryType;
import com.oocourse.uml2.interact.exceptions.user.AttributeDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.AttributeNotFoundException;
import com.oocourse.uml2.interact.exceptions.user.ClassDuplicatedException;
import com.oocourse.uml2.interact.exceptions.user.ClassNotFoundException;
import com.oocourse.uml2.interact.format.UmlClassModelInteraction;
import com.oocourse.uml2.models.common.Visibility;
import com.oocourse.uml2.models.elements.UmlAssociation;
import com.oocourse.uml2.models.elements.UmlAssociationEnd;
import com.oocourse.uml2.models.elements.UmlAttribute;
import com.oocourse.uml2.models.elements.UmlElement;
import com.oocourse.uml2.models.elements.UmlGeneralization;
import com.oocourse.uml2.models.elements.UmlInterfaceRealization;
import com.oocourse.uml2.models.elements.UmlOperation;
import com.oocourse.uml2.models.elements.UmlParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyUmlClassInteraction implements UmlClassModelInteraction {

    private final HashMap<String, HashSet<String>> classSet = new HashMap<>();
    private final HashMap<String, ClassInfo> classMap = new HashMap<>();
    private final HashMap<String, HashSet<String>>
            interfaces = new HashMap<>(); // extends
    private final HashMap<String, String> interfaceMap = new HashMap<>();
    private final HashMap<String, Boolean> interfaceDup = new HashMap<>();
    private final HashMap<String, String> endMap = new HashMap<>();
    private final HashMap<String, String> endNames = new HashMap<>();
    private final HashMap<String, String> optMap = new HashMap<>();
    private final HashMap<String, UmlElement> elementMap = new HashMap<>();
    private int classCount = 0;

    MyUmlClassInteraction(UmlElement... elements) {
        for (UmlElement element : elements) {
            switch (element.getElementType()) {
                case UML_CLASS:
                    elementMap.put(element.getId(), element);
                    addClass(element);
                    break;
                case UML_INTERFACE:
                    elementMap.put(element.getId(), element);
                    addInterface(element);
                    break;
                case UML_ASSOCIATION_END:
                    addAssociationEnd(element);
                    break;
                case UML_OPERATION:
                    optMap.put(element.getId(), element.getParentId());
                    elementMap.put(element.getId(), element);
                    /*System.err.println(element.getId() + ": " +
                            element.getName());*/
                    break;
                default:
            }
        }
        for (String optId : optMap.keySet()) {
            addOpt(elementMap, optId);
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

    HashMap<String, ClassInfo> getClassMap() {
        return classMap;
    }

    HashMap<String, HashSet<String>> getInterfaces() {
        return interfaces;
    }

    HashMap<String, UmlElement> getElementMap() {
        return elementMap;
    }

    HashMap<String, Boolean> getInterfaceDup() {
        return interfaceDup;
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
        } else if (!interfaces.get(source).add(target)) {
            interfaceDup.put(source, true);
        }
    }

    private void addAss(UmlAssociation association) {
        String id1 = endMap.get(association.getEnd1());
        String id2 = endMap.get(association.getEnd2());
        String name1 = endNames.get(association.getEnd1());
        String name2 = endNames.get(association.getEnd2());
        if (classMap.containsKey(id1)) {
            classMap.get(id1).addAssociation(name2,
                    id2, classMap.containsKey(id2));
        }
        if (classMap.containsKey(id2)) {
            classMap.get(id2).addAssociation(name1,
                    id1, classMap.containsKey(id1));
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
        endNames.put(element.getId(), element.getName());
    }

    private void addInterface(UmlElement element) {
        interfaces.put(element.getId(), new HashSet<>());
        interfaceMap.put(element.getId(), element.getName());
        interfaceDup.put(element.getId(), false);
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
                info.addAssociation("", id, true);
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

    private final HashSet<String> vis = new HashSet<>();

    void interfaceAddSuper(String id) {
        if (vis.contains(id) || interfaces.get(id).isEmpty()) {
            vis.add(id);
            return;
        }
        vis.add(id);
        HashSet<String> set = interfaces.get(id);
        LinkedList<String> list = new LinkedList<>(set);
        boolean flag = false;
        for (String superId : list) {
            interfaceAddSuper(superId);
            for (String str : interfaces.get(superId)) {
                flag = !set.add(str) || interfaceDup.get(superId) || flag;
            }
        }
        interfaceDup.put(id, interfaceDup.get(id) || flag);
    }

    void addInterfaceGroup(ClassInfo info) {
        if (info.isInterfacesCompleted()) {
            return;
        }
        info.hasInterfacesCompleted();
        LinkedList<String> list = info.getInterfaceIds();
        ArrayList<String> arrayList = new ArrayList<>(list);
        for (String iid : arrayList) {
            interfaceAddSuper(iid);
            for (String sid : interfaces.get(iid)) {
                info.addInterface(sid, interfaceMap.get(sid));
            }
        }
    }

    void classAddSuperInterface(String classId) {
        ClassInfo info = classMap.get(classId);
        if (info.isSuperInterfaceIncluded()) {
            return;
        }
        info.superHasInterfaceIncluded();
        String parentId = info.getSuperClassId();
        if (parentId != null) {
            classAddSuperInterface(parentId);
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
            classAddSuperInterface(id);
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
