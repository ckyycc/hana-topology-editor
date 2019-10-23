package com.sap.hana.topology.util;

import com.sap.hana.topology.tree.TTNode;

/**
 * Utilities for Topology Tree
 */
public final class TreeUtils {
    /**
     * Topology tree leaf node delimiter of name and value.
     */
    public final static String NAME_VALUE_DELIMITER = " >> ";

    /**
     * Id of topology tree exporter
     */
    public final static String TOPOLOGY_TREE_EXPORTER_ID = "export_hdbnsutil";

    /**
     * Name for the root name of topology tree
     */
    public final static String TOPOLOGY_TREE_ROOT_NAME = "topology";


    /**
     * Get topology node via name from parent node, only check 1 level, no need to do it recursively.
     * @param parent parent node
     * @param name name of the topology node
     * @return topology node
     */
    public static TTNode<String> getTopologyNodeFromParent(TTNode<String> parent, String name) {
        if (parent != null && !CommonUtils.isNullOrEmpty(name)) {
            for (TTNode<String> node : parent.getChildren()) {
                if (name.equalsIgnoreCase(node.getName())) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * get parent node by level, only apply to parents (provided level <= current)
     */
    public static TTNode<String> getParentByLevel(TTNode<String> node, int lvl) throws TTException {
        if (node == null) {
            throw new TTException("Can not find the parent for the null node.");
        }
        if (lvl <= 0) {
            throw new TTException("Can not get the parent for the root node.");
        }
        if (lvl == node.getLevel()) {
            return node.getParent();
        } else if (lvl < node.getLevel()) {
            TTNode<String> parentNode = node.getParent();
            while (parentNode.getLevel() > lvl) {
                parentNode = parentNode.getParent();
            }
            return parentNode.getParent();
        } else {
            throw new TTException("Can not get the parent by the smaller level.");
        }
    }
}
