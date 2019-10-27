package com.sap.hana.topology.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Node of the topology tree. */
public final class TTNode<T> {

  /** Delimiter between different nodes, it's using for node id */
  private static final String ID_DELIMITER = "/";

  /** id of the node, id is the full path of the node */
  private String id;

  /** Name of the node */
  private T name;

  /** Value of the node */
  private T value;

  /**
   * flag of leaf node. Note: leaf here is different with the "leaf" of a normal tree, leaf node in
   * the topology tree means it contains value only non-leaf node in the topology tree means it can
   * contain sub-node(s) only So, a non-leaf node can be a node which doesn't have either sub-node,
   * but can't add value to it a leaf node can be a node which doesn't have value, but can't add
   * sub-node to it
   */
  private boolean isLeaf;

  /** level of the node, level of root is 0 */
  private int level;

  /** Children list, this list is null if the node is leaf */
  private List<TTNode<T>> children;

  /** Parent node, it is null if the node is root */
  private TTNode<T> parent;

  /** Init the empty children list */
  private TTNode() {
    // make sure children is not null
    this.children = new ArrayList<>();
  }

  /** Create a root node with name */
  public TTNode(T name) {
    this();
    this.id = ID_DELIMITER;
    this.name = name;
    this.isLeaf = false;
    this.level = 0;
  }

  /** Create a non-leaf node with parent and name */
  public TTNode(TTNode<T> parent, T name) {
    this();
    this.id = getTopologyPath(parent, name);
    this.name = name;
    this.level = parent.level + 1;
    this.isLeaf = false;
  }

  /** Create a leaf with parent, name and value */
  public TTNode(TTNode<T> parent, T name, T value) {
    this();
    this.id = getTopologyPath(parent, name);
    this.name = name;
    this.value = value;
    this.level = parent.level + 1;
    this.isLeaf = true;
  }

  /** Create a non-leaf node with id and name */
  public TTNode(String id, T name) {
    this();
    this.id = id;
    this.name = name;
    this.isLeaf = false;
  }

  /** Create a leaf node with id, name and value */
  public TTNode(String id, T name, T value) {
    this();
    this.id = id;
    this.name = name;
    this.value = value;
    this.isLeaf = true;
  }

  /** Get id of current node */
  public String getId() {
    return id;
  }

  /** Get Level of current node; */
  public int getLevel() {
    return level;
  }

  /** Get name of current node; */
  public T getName() {
    return name;
  }

  /**
   * Set name of to current node, and update id accordingly.
   *
   * @param name name of current node
   */
  public void setName(T name) {
    if (!this.name.equals(name)) {
      this.name = name;
      updateId(); // name changed, id will be changed as well
    }
  }

  /** Get value of current node; */
  public T getValue() {
    return value;
  }

  /**
   * Set value to the node, and set the leaf flag accordingly
   *
   * @param value value of the node
   */
  public void setValue(T value) {
    if (this.children.size() == 0) {
      this.value = value;
      this.isLeaf = true;
    } else {
      throw new RuntimeException("Can not set value to a node which contains sub node!");
    }
  }

  /**
   * Current node is a leaf node or not.
   *
   * @return leaf node flag
   */
  public boolean isLeaf() {
    return isLeaf;
  }

  /**
   * Set leaf property for the node, for non-leaf node, the value will be set to null
   *
   * @param leaf leaf flag
   */
  public void setLeaf(boolean leaf) {
    if (leaf && this.children.size() > 0) {
      throw new RuntimeException("Can not change a node to leaf when it contains sub-nodes!");
    }

    if (!leaf) {
      this.value = null;
    }
    this.isLeaf = leaf;
  }

  /**
   * Current node is a root node or not.
   *
   * @return true: it is a root node; false: it's not a root node;
   */
  public boolean isRoot() {
    return parent == null;
  }

  /**
   * Get root node
   *
   * @return root node
   */
  public TTNode<T> getRoot() {
    TTNode<T> root = this;

    while (root.parent != null) {
      root = root.parent;
    }

    return root;
  }

  /** Get Parent of current node */
  public TTNode<T> getParent() {
    return parent;
  }

  /** Get Children list of current node */
  public List<TTNode<T>> getChildren() {
    return children;
  }

  /**
   * Set parent to current node
   *
   * @param parent parent of current node
   */
  public void setParent(TTNode<T> parent) {
    this.parent = parent;
  }

  /**
   * Add child node to current node
   *
   * @param child child node to be added
   */
  public void addChild(TTNode<T> child) {
    if (isLeaf) {
      throw new RuntimeException("Can not add child to a LEAF node.");
    }

    child.level = level + 1;
    child.parent = this;
    children.add(child);
  }

  /**
   * Delete child node from current node
   *
   * @param child child node to be deleted
   */
  public void deleteChild(TTNode<T> child) {
    children.remove(child);
  }

  /**
   * Get topology path via parent node, the path = parent.id + "/" + name of current node
   *
   * @param parent parent node
   * @param name current node name, to get a better path, this object needs to implement toString()
   * @return the topology path
   */
  private String getTopologyPath(TTNode<T> parent, T name) {
    return parent.getId().equals(ID_DELIMITER)
        ? parent.getId() + name
        : parent.getId() + ID_DELIMITER + name;
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

  /** Update id using parent node and current name */
  private void updateId() {
    id = getTopologyPath(parent, name);

    // update id for all children
    for (TTNode<T> node : children) {
      node.updateId();
    }
  }
}
