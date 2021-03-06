package com.sap.hana.topology.tree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TTNodeTest {

  @Test
  void addChild_LeafNode_ShouldThrowException() {
    TTNode<String> node = new TTNode<>("ID", "Name", "Value");
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> node.addChild(new TTNode<>("ID1", "Name1", "Value1")));
    assertTrue(thrown.getMessage().contains("Can not add child to a LEAF node"));
  }

  @Test
  void addChild_ShouldAddNodeToChildren() {
    TTNode<String> node = new TTNode<>("ID", "Name");
    TTNode<String> child = new TTNode<>("ID1", "Name1", "Value1");
    node.addChild(child);
    assertTrue(node.getChildren().contains(child));
  }

  @Test
  void addChild_ShouldMaintainThePropertiesOfTheChild() {
    TTNode<String> node = new TTNode<>("ID", "Name");
    TTNode<String> child = new TTNode<>("ID1", "Name1", "Value1");
    node.addChild(child);

    assertAll(
        () -> assertTrue(node.getChildren().contains(child)),
        () -> assertEquals(child.getLevel(), node.getLevel() + 1),
        () -> assertEquals(child.getParent(), node));
  }

  @Test
  void setValue_ShouldThrowExceptionIfChildrenListIsNotEmpty() {
    TTNode<String> node = new TTNode<>("ID", "Name");
    TTNode<String> child = new TTNode<>("ID1", "Name1", "Value1");
    node.addChild(child);

    assertThrows(RuntimeException.class, () -> node.setValue("TEST"));
  }

  @Test
  void setValue_ShouldSetValueAndLeafFlagIfChildrenListIsEmpty() {
    TTNode<String> node = new TTNode<>("ID", "Name");
    String newValue = "NEW_TEST_VALUE";
    node.setValue(newValue);

    assertAll(() -> assertEquals(newValue, node.getValue()), () -> assertTrue(node.isLeaf()));
  }
}
