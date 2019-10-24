package com.sap.hana.topology.util;

import static org.junit.jupiter.api.Assertions.*;

import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.ui.tree.FilterableTreeItem;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UIUtilsTest {

  @ParameterizedTest
  @MethodSource("getTreeNodeValue4DisplayArgFactory")
  void getTreeNodeValue4Display_AllNames_Success(String name) {
    TTNode<String> node = new TTNode<>("TEST_ID", name, "TEST_VALUE");
    String res = UIUtils.getTreeNodeValue4Display(node);

    assertEquals(res, name);
  }

  @Test
  void buildTree_NullTreeItem_DoesNothing() {
    Map<TreeItem<String>, TTNode<String>> treeViewMap = new HashMap<>();

    // no exception should be threw
    assertDoesNotThrow(
        () ->
            UIUtils.buildTree(
                null, new TTNode<>("TEST_ID", "TEST_NAME", "TEST_VALUE"), treeViewMap));
    assertEquals(0, treeViewMap.size());
  }

  @Test
  void buildTree_NullTreeNode_DoesNothing() {
    Map<TreeItem<String>, TTNode<String>> treeViewMap = new HashMap<>();
    // no exception should be threw
    assertDoesNotThrow(
        () -> UIUtils.buildTree(new FilterableTreeItem<>("TEST", "TEST"), null, treeViewMap));
    assertEquals(0, treeViewMap.size());
  }

  @Test
  void buildTree_NullTreeMap_ThrowsTTException() {
    Map<TreeItem<String>, TTNode<String>> treeViewMap = null;
    TTException thrown =
        assertThrows(
            TTException.class,
            () ->
                UIUtils.buildTree(
                    new FilterableTreeItem<>("TEST", "TEST"),
                    new TTNode<>("TEST_ID", "TEST_NAME", "TEST_VALUE"),
                    treeViewMap));
    assertTrue(thrown.getMessage().contains("Internal error occurred"));
  }

  @Test
  void buildTree_OneLevelTreeNode_Success() {
    Map<TreeItem<String>, TTNode<String>> treeViewMap = new HashMap<>();
    TTNode<String> node = new TTNode<>("TEST_ID", "TEST_NAME", "TEST_VALUE");

    TreeItem<String> subNode = new FilterableTreeItem<>(node.getName(), "TEST");

    assertDoesNotThrow(
        () -> UIUtils.buildTree(new FilterableTreeItem<>("TEST", "TEST"), node, treeViewMap));

    assertEquals(1, treeViewMap.size());
    TreeItem<String> item = treeViewMap.keySet().iterator().next();

    assertEquals(item.getValue(), subNode.getValue());
    assertEquals(treeViewMap.entrySet().iterator().next().getValue(), node);
  }

  @Test
  void buildTree_MultiLevelTreeNode_Success() {
    // build a test tree node
    TTNode<String> root = new TTNode<>("TEST_ID1", "TEST_NAME1");
    TTNode<String> child1 = new TTNode<>("TEST_ID2", "TEST_NAME2");
    root.addChild(child1);
    TTNode<String> child2 = new TTNode<>("TEST_ID3", "TEST_NAME3", "TEST_VALUE3");
    child1.addChild(child2);

    Map<TreeItem<String>, TTNode<String>> treeViewMap = new HashMap<>();
    // build the test tree items
    TreeItem<String> treeNode1 = new FilterableTreeItem<>(root.getName(), "TEST");
    TreeItem<String> treeNode2 = new FilterableTreeItem<>(child1.getName(), "TEST");
    TreeItem<String> treeNode3 = new FilterableTreeItem<>(child2.getName(), "TEST");

    // build the test map
    Map<String, TTNode<String>> testMap = new HashMap<>();
    testMap.put(treeNode1.getValue(), root);
    testMap.put(treeNode2.getValue(), child1);
    testMap.put(treeNode3.getValue(), child2);

    assertDoesNotThrow(
        () -> UIUtils.buildTree(new FilterableTreeItem<>("TEST", "TEST"), root, treeViewMap));
    assertEquals(3, treeViewMap.size());

    // the result should be the same with the test map
    for (Map.Entry<TreeItem<String>, TTNode<String>> entry : treeViewMap.entrySet()) {
      assertTrue(testMap.containsKey(entry.getKey().getValue()));
      assertEquals(entry.getValue(), testMap.get(entry.getKey().getValue()));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"test_value", ""})
  void getId4FilterableTreeItem_ShouldReturnIDPlusValueForLeadNodeIfValueIsNotNull(String value) {
    String name = "TEST_NAME";
    TTNode<String> node = new TTNode<>("/" + name, name, value);

    assertEquals(UIUtils.getId4FilterableTreeItem(node), node.getId() + "/" + node.getValue());
  }

  @Test
  void getId4FilterableTreeItem_ShouldReturnIDForLeadNodeIfValueIsNull() {
    String name = "TEST_NAME", value = null;
    TTNode<String> node = new TTNode<>("/" + name, name, value);

    assertEquals(UIUtils.getId4FilterableTreeItem(node), node.getId());
  }

  @Test
  void getId4FilterableTreeItem_ShouldReturnIDForNonLeadNode() {
    String name = "TEST_NAME";
    TTNode<String> node = new TTNode<>("/" + name, name);

    assertEquals(UIUtils.getId4FilterableTreeItem(node), node.getId());
  }

  private static Stream<String> getTreeNodeValue4DisplayArgFactory() {
    return Stream.of("", null, "TEST_NAME");
  }
}
