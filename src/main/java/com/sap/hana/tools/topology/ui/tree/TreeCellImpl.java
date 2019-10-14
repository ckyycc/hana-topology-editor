package com.sap.hana.tools.topology.ui.tree;

import com.sap.hana.tools.topology.tree.TTNode;
import com.sap.hana.tools.topology.util.TreeUtils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Map;

/**
 * Implementation of tree cell, modified the display logic for leaf node:
 * for leaf node, the value and name will be displayed together
 */
public class TreeCellImpl extends TreeCell<String> {
    private Map<TreeItem<String>, TTNode> tvMap;
    public TreeCellImpl(Map<TreeItem<String>, TTNode> treeViewMap) {
        tvMap = treeViewMap;
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        if (tvMap.containsKey(getTreeItem()) && tvMap.get(getTreeItem()).isLeaf()) {
            setText(null);
            setGraphic(createLeafNode());
        }
        else {
            setText(item);
            setGraphic(getTreeItem().getGraphic());
        }
    }


    /**
     * Create leaf node
     * @return leaf node
     */
    private Node createLeafNode() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);

        Label tvLeafName = new Label(tvMap.get(getTreeItem()).getName());
        tvLeafName.getStyleClass().add("lb-leaf-name");

        Label tvLeafDelimiter = new Label(TreeUtils.NAME_VALUE_DELIMITER);
        tvLeafDelimiter.getStyleClass().add("icon-leaf-name-value-delimiter");

        Label tvLeafValue = new Label(tvMap.get(getTreeItem()).getValue());
        tvLeafValue.getStyleClass().add("lb-leaf-value");

        container.getChildren().addAll(tvLeafName, tvLeafDelimiter, tvLeafValue);
        return container;
    }
}
