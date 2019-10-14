package com.sap.hana.topology.util;

import com.sap.hana.topology.tree.TTNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TreeUtilsTest {

    @ParameterizedTest
    @MethodSource("getTopologyNodeFromParentArgFactoryEmptyNode")
    void getTopologyNodeFromParent_EmptyNodeOrEmptyName_ShouldReturnNull(TTNode parentNode, String name) {
        TTNode node = TreeUtils.getTopologyNodeFromParent(parentNode, name);
        assertNull(node);
    }
    private static Stream<Arguments> getTopologyNodeFromParentArgFactoryEmptyNode() {
        //null parent node
        Arguments t1 = arguments(null, "name");
        //parent node with no child
        Arguments t2 = arguments(new TTNode("id", "name"), "name");
        //normal node with empty name
        TTNode node = new TTNode("id", "name");
        node.addChild(new TTNode("cid", "cname"));
        Arguments t3 = arguments(node, "");
        //normal node with null name
        Arguments t4 = arguments(node, null);

        return Stream.of(t1, t2, t3, t4);
    }

    @Test
    void getTopologyNodeFromParent_ShouldReturnTheSubNodeIfFound() {
        String name = "childName";
        TTNode parentNode = new TTNode("id", "name");
        TTNode childNode1 = new TTNode("cid", name);
        TTNode childNode2 = new TTNode("cid2", name + "2");

        parentNode.addChild(childNode1);
        parentNode.addChild(childNode2);
        TTNode node = TreeUtils.getTopologyNodeFromParent(parentNode, name);
        assertEquals(childNode1, node);
    }

    @Test
    void getTopologyNodeFromParent_ShouldReturnNullIfNotFound() {
        String name = "childName";
        TTNode parentNode = new TTNode("id", "name");
        TTNode childNode1 = new TTNode("cid", name + "1");
        TTNode childNode2 = new TTNode("cid2", name + "2");

        parentNode.addChild(childNode1);
        parentNode.addChild(childNode2);

        TTNode node = TreeUtils.getTopologyNodeFromParent(parentNode, name);
        assertNull(node);
    }

    @Test
    void getParentByLevel_ShouldThrowExceptionIfNullNode() {
        TTException thrown = assertThrows(TTException.class, () -> TreeUtils.getParentByLevel(null, 1));
        assertTrue(thrown.getMessage().contains("Can not find the parent for the null node"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void getParentByLevel_ShouldThrowExceptionIfLevelLessOrEquals0(int level) {
        TTException thrown = assertThrows(TTException.class, () -> TreeUtils.getParentByLevel(new TTNode("ID", "Name"), level));
        assertTrue(thrown.getMessage().contains("Can not get the parent for the root node"));
    }

    @Test
    void getParentByLevel_ShouldThrowExceptionIfLevelNumberBiggerThanCurrentNode() {
        TTNode node = new TTNode("ROOT"); //level 0
        TTNode child = new TTNode(node, "CHILD"); //level 1
        node.addChild(child);
        TTException thrown = assertThrows(TTException.class, () -> TreeUtils.getParentByLevel(child, 2));
        assertTrue(thrown.getMessage().contains("Can not get the parent by the smaller level"));
    }

    @Test
    void getParentByLevel_ShouldGetParentNodeIfLevelEqualsCurrentNode() {
        TTNode node = new TTNode("ROOT"); //level 0
        TTNode child = new TTNode(node, "CHILD"); //level 1
        node.addChild(child);
        TTNode result = assertDoesNotThrow(() -> TreeUtils.getParentByLevel(child, 1));
        assertEquals(node, result);
    }

    @Test
    void getParentByLevel_ShouldGetParentNodeIfLevelLessThanCurrentNode() {
        TTNode node = new TTNode("ROOT"); //level 0
        TTNode child = new TTNode(node, "CHILD"); //level 1
        TTNode grandchild = new TTNode(child, "GRANDCHILD"); //level 2

        node.addChild(child);
        child.addChild(grandchild);

        TTNode result = assertDoesNotThrow(() -> TreeUtils.getParentByLevel(grandchild, 1));
        assertEquals(node, result);
    }
}
