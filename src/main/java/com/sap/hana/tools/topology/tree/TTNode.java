package com.sap.hana.tools.topology.tree;

import com.sap.hana.tools.topology.exception.TTRTException;
import com.sap.hana.tools.topology.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node of the topology tree.
 */
public class TTNode {

    /**
     * Delimiter between different nodes, it's using for node id
     */
    private final static String ID_DELIMITER = "/";

    /**
     * id of the node, id is the full path of the node
     */
    private String id;

    /**
     * Name of the node
     */
    private String name;

    /**
     * Value of the node
     */
    private String value;

    /**
     * flag of leaf node
     */
    private boolean isLeaf;

    /**
     * level of the node, level of root is 0
     */
    private int level;

    /**
     * Children list, this list is null if the node is leaf
     */
    private List<TTNode> children;

    /**
     * Parent node, it is null if the node is root
     */
    private TTNode parent;

    /**
     * Create a root node with name
     */
    public TTNode(String name) {
        this.id = ID_DELIMITER;
        this.name = name;
        this.isLeaf = false;
        this.level = 0;
    }

    /**
     * Create a non-leaf node with parent and name
     */
    public TTNode(TTNode parent, String name) {
        this.id = getTopologyPath(parent, name);
        this.name = name;
        this.level = parent.level + 1;
        this.isLeaf = false;
    }

    /**
     * Create a leaf with parent, name and value
     */
    public TTNode(TTNode parent, String name, String value) {
        this.id = getTopologyPath(parent, name);
        this.name = name;
        this.value = value;
        this.level = parent.level + 1;
        this.isLeaf = true;
    }

    /**
     * Create a non-leaf node with id and name
     */
    public TTNode(String id, String name) {
        this.id = id;
        this.name = name;
        this.isLeaf = false;
    }

    /**
     * Create a leaf node with id, name and value
     */
    public TTNode(String id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.isLeaf = true;
    }

    /**
     * Get id of current node
     */
    public String getId() {
        return id;
    }

    /**
     * Get Level of current node;
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get name of current node;
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of to current node, and update id accordingly.
     * @param name name of current node
     */
    public void setName(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            updateId(); //name changed, id will be changed as well
        }
    }

    /**
     * Get value of current node;
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value to the node, and set the leaf flag accordingly
     * @param value value of the node
     */
    public void setValue(String value) {
        if (CommonUtils.isNullOrEmpty(this.children)) {
            this.value = value;
            this.isLeaf = true;
        }
    }

    /**
     * Current node is a leaf node or not.
     * @return leaf node flag
     */
    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Current node is a root node or not.
     * @return true: it is a root node; false: it's not a root node;
     */
    public boolean isRoot() { return parent == null;}

    /**
     * Get root node
     * @return root node
     */
    public TTNode getRoot() {
        TTNode root = this;

        while (root.parent != null) {
            root = root.parent;
        }

        return root;
    }

    /**
     * Get Parent of current node
     */
    public TTNode getParent() {
        return parent;
    }

    /**
     * Get Children list of current node
     */
    public List<TTNode> getChildren() {
        return children;
    }

    /**
     * Set parent to current node
     * @param parent parent of current node
     */
    public void setParent(TTNode parent) {
        this.parent = parent;
    }

    /**
     * Add child node to current node
     * @param child child node to be added
     */
    public void addChild(TTNode child) {
        if (isLeaf) {
            throw new TTRTException("Can not add child to a LEAF node.");
        }

        if (CommonUtils.isNullOrEmpty(children)) {
            children = new ArrayList<>();
        }
        child.level = level + 1;
        child.parent = this;
        children.add(child);
    }

    /**
     * Delete child node from current node
     * @param child child node to be deleted
     */
    public void deleteChild(TTNode child) {
        if (!CommonUtils.isNullOrEmpty(children)) {
            children.remove(child);
        }
    }

//    /**
//     * get parent node by level, only apply to parents (provided level <= current)
//     */
//    public TopologyTreeNode getParentByLevel(int lvl) throws TTException {
//        if (lvl <= 0) {
//            throw new TTException("Can not get the parent for the root node.");
//        }
//        if (lvl == level) {
//            return parent;
//        } else if (lvl < level) {
//            TopologyTreeNode parentNode = parent;
//            while (parentNode.level > lvl) {
//                parentNode = parentNode.parent;
//            }
//            return parentNode.parent;
//        } else {
//            throw new TTException("Can not get the parent by the smaller level.");
//        }
//    }

    /**
     * Get topology path via parent node, the path = parent.id + "/" + name of current node
     * @param parent parent node
     * @param name current node name
     * @return the topology path
     */
    private String getTopologyPath(TTNode parent, String name) {
        return parent.getId().equals(ID_DELIMITER) ? parent.getId() + name : parent.getId() + ID_DELIMITER + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TTNode)) return false;
        TTNode that = (TTNode) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }

    /**
     * Update id using parent node and current name
     */
    private void updateId() {
        this.id = getTopologyPath(parent, name);
    }
}
