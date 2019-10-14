package com.sap.hana.topology.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TTNodeTest {

    @Test
    void addChild_LeafNode_ShouldThrowException() {
        TTNode node = new TTNode("ID", "Name", "Value");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> node.addChild(new TTNode("ID1", "Name1", "Value1")));
        assertTrue(thrown.getMessage().contains("Can not add child to a LEAF node"));
    }

    @Test
    void addChild_ShouldAddNodeToChildren() {
        TTNode node = new TTNode("ID", "Name");
        TTNode child = new TTNode("ID1", "Name1", "Value1");
        node.addChild(child);
        assertTrue(node.getChildren().contains(child));
    }

    @Test
    void addChild_ShouldMaintainThePropertiesOfTheChild() {
        TTNode node = new TTNode("ID", "Name");
        TTNode child = new TTNode("ID1", "Name1", "Value1");
        node.addChild(child);

        assertAll(() -> assertTrue(node.getChildren().contains(child)),
                () -> assertEquals(child.getLevel(), node.getLevel() + 1),
                () -> assertEquals(child.getParent(), node));
    }

    @Test
    void setValue_ShouldDoNothingIfChildrenListIsNotEmpty() {
        TTNode node = new TTNode("ID", "Name");
        TTNode child = new TTNode("ID1", "Name1", "Value1");
        node.addChild(child);

        String oldValue = node.getValue();
        boolean oldLeafFlag = node.isLeaf();
        node.setValue("TEST");
        assertAll(() -> assertEquals(oldValue, node.getValue()), () -> assertEquals(oldLeafFlag, node.isLeaf()));
    }

    @Test
    void setValue_ShouldSetValueAndLeafFlagIfChildrenListIsEmpty() {
        TTNode node = new TTNode("ID", "Name");
        String newValue = "NEW_TEST_VALUE";
        node.setValue(newValue);

        assertAll(() -> assertEquals(newValue, node.getValue()), () -> assertTrue(node.isLeaf()));
    }
}
