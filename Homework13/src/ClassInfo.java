import com.oocourse.uml1.interact.common.AttributeClassInformation;
import com.oocourse.uml1.interact.common.OperationQueryType;
import com.oocourse.uml1.interact.exceptions.user.AttributeDuplicatedException;
import com.oocourse.uml1.interact.exceptions.user.AttributeNotFoundException;
import com.oocourse.uml1.models.common.Direction;
import com.oocourse.uml1.models.common.Visibility;
import com.oocourse.uml1.models.elements.UmlAttribute;
import com.oocourse.uml1.models.elements.UmlOperation;
import com.oocourse.uml1.models.elements.UmlParameter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ClassInfo {

    private final String className;
    private String superClassId;
    private String topClassId = null;
    private String topClassName = null;

    private final LinkedList<String> interfaces = new LinkedList<>();
    private final LinkedList<String> interfaceIds = new LinkedList<>();
    private final HashMap<String, String> interfaceMap = new HashMap<>();
    private final HashSet<String> assSet = new HashSet<>();
    private LinkedList<String> assList = null;
    private final HashMap<String, LinkedList<UmlAttribute>>
            attributes = new HashMap<>();
    private final HashMap<String, LinkedList<UmlOperation>>
            operations = new HashMap<>();
    private final HashSet<String> optSet = new HashSet<>();
    private final LinkedList<AttributeClassInformation>
            notHiddenList = new LinkedList<>();

    private int numOfAttr = 0;
    private int numOfSelfAttr = 0;
    private int numOfAss = 0;
    private int numOfOpt = 0;
    private final HashMap<OperationQueryType, HashSet<String>>
            optSta = new HashMap<>();

    private boolean superAttrIncluded = false;
    private boolean superAssIncluded = false;
    private boolean superInterfaceIncluded = false;
    private boolean interfacesCompleted = false;

    ClassInfo(String name) {
        className = name;
        superClassId = null;
        optSta.put(OperationQueryType.NON_RETURN, new HashSet<>());
        optSta.put(OperationQueryType.NON_PARAM, new HashSet<>());
        optSta.put(OperationQueryType.RETURN, new HashSet<>());
        optSta.put(OperationQueryType.PARAM, new HashSet<>());
    }

    public String getClassName() {
        return className;
    }

    public boolean isSuperAttrIncluded() {
        return superAttrIncluded;
    }

    void superHasAttrIncluded() {
        superAttrIncluded = true;
    }

    public boolean isSuperAssIncluded() {
        return superAssIncluded;
    }

    void superHasAssIncluded() {
        superAssIncluded = true;
    }

    public boolean isSuperInterfaceIncluded() {
        return superInterfaceIncluded;
    }

    public boolean isInterfacesCompleted() {
        return interfacesCompleted;
    }

    void hasInterfacesCompleted() {
        interfacesCompleted = true;
    }

    void superHasInterfaceIncluded() {
        superInterfaceIncluded = true;
    }

    public String getSuperClassId() {
        return superClassId;
    }

    void setSuperClassId(String superClassId) {
        this.superClassId = superClassId;
    }

    public String getTopClassId() {
        return topClassId;
    }

    void setTopClassId(String topClassId) {
        this.topClassId = topClassId;
    }

    public String getTopClassName() {
        return topClassName;
    }

    void setTopClassName(String topClassName) {
        this.topClassName = topClassName;
    }

    public int getNumOfAttr() {
        return numOfAttr;
    }

    public int getNumOfSelfAttr() {
        return numOfSelfAttr;
    }

    public int getNumOfAss() {
        return numOfAss;
    }

    void setNumOfAss(int numOfAss) {
        this.numOfAss = numOfAss;
    }

    void ass() {
        numOfAss++;
    }

    LinkedList<String> getAssList() {
        return assList;
    }

    void setAssList(LinkedList<String> assList) {
        this.assList = assList;
    }

    public Visibility getAttributeVisibility(String attr)
            throws AttributeNotFoundException, AttributeDuplicatedException {
        if (!attributes.containsKey(attr)) {
            throw new AttributeNotFoundException(className, attr);
        }
        if (attributes.get(attr).size() > 1) {
            throw new AttributeDuplicatedException(className, attr);
        }
        return attributes.get(attr).getFirst().getVisibility();
    }

    HashMap<String, LinkedList<UmlOperation>> getOperations() {
        return operations;
    }

    boolean addInterface(String interfaceId, String interfaceName) {
        if (!interfaceMap.containsKey(interfaceId)) {
            interfaces.addLast(interfaceName);
            interfaceIds.addLast(interfaceId);
            interfaceMap.put(interfaceId, interfaceName);
            return true;
        }
        return false;
    }

    LinkedList<String> getInterfaces() {
        return interfaces;
    }

    HashMap<String, String> getInterfaceMap() {
        return interfaceMap;
    }

    LinkedList<String> getInterfaceIds() {
        return interfaceIds;
    }

    void addAttr(UmlAttribute attribute, String cn) {
        String str = attribute.getName();
        if (!attributes.containsKey(str)) {
            attributes.put(str, new LinkedList<>());
        }
        attributes.get(str).addLast(attribute);
        if (!attribute.getVisibility().equals(Visibility.PRIVATE)) {
            notHiddenList.addLast(
                    new AttributeClassInformation(str, cn));
        }
        numOfAttr++;
        if (!superAttrIncluded) {
            numOfSelfAttr++;
        }
    }

    void addAssociation(String classId, boolean isClass) {
        if (isClass) {
            assSet.add(classId);
        }
    }

    HashSet<String> getAssSet() {
        return assSet;
    }

    void addOperation(UmlOperation operation) {
        if (!operations.containsKey(operation.getName())) {
            operations.put(operation.getName(), new LinkedList<>());
        }
        operations.get(operation.getName()).addLast(operation);
        numOfOpt++;
        optSet.add(operation.getId());
        optSta.get(OperationQueryType.NON_PARAM).add(operation.getId());
        optSta.get(OperationQueryType.NON_RETURN).add(operation.getId());
    }

    void addPara(UmlParameter parameter) {
        String optId = parameter.getParentId();
        if (!optSet.contains(optId)) {
            return;
        }
        Direction direction = parameter.getDirection();
        if (direction == Direction.RETURN) {
            optSta.get(OperationQueryType.NON_RETURN).remove(optId);
            optSta.get(OperationQueryType.RETURN).add(optId);
        } else {
            optSta.get(OperationQueryType.NON_PARAM).remove(optId);
            optSta.get(OperationQueryType.PARAM).add(optId);
        }
    }

    public int getOptCnt(OperationQueryType type) {
        if (type == OperationQueryType.ALL) {
            return numOfOpt;
        } else {
            return optSta.get(type).size();
        }
    }

    LinkedList<AttributeClassInformation> getNotHiddenList() {
        return notHiddenList;
    }

    HashMap<String, LinkedList<UmlAttribute>> getAttributes() {
        return attributes;
    }

}
