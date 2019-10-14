package com.sap.hana.tools.topology.tree.controller;

import com.sap.hana.tools.topology.exception.TTException;
import com.sap.hana.tools.topology.tree.TTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TTControllerImplTest {

    private TTController ttController = TTControllerImpl.getInstance();

    private final static String str1 = "s>topology3\nn1>host\nn2>vanpghana11\ns<topology3\n";
    private final static String str2 = "{\n\"topology\":{\n\"host\":{\"vanpghana11\":{}}}}";
    private final static String str3 = "''=\n  host=\n    vanpghana11";
    private final static String str4 = "''\n  host\n    vanpghana11";

    private final static TTNode root = new TTNode("topology");
    private final static TTNode host = new TTNode(root, "host");
    private final static TTNode server = new TTNode(host, "vanpghana11");

    @BeforeAll
    static void init() {
        root.addChild(host);
        host.addChild(server);
    }

    @Test
    void loadTopology_ShouldThrowExceptionIfNoProcessorFound() {
        TTException thrown = assertThrows(TTException.class, () -> ttController.loadTopology("TEST_NO_PROCESSOR_MATCH"));
        assertTrue(thrown.getMessage().contains("File format is not supported"));
    }

    @ParameterizedTest
    @MethodSource("loadTopologyArgFactory")
    void loadTopology_ShouldReturnTreeNode(String topologyStr) {
        TTNode node = assertDoesNotThrow(() -> ttController.loadTopology(topologyStr));

        assertAll(() -> assertEquals(node, root),
                () -> assertEquals(node.getChildren().get(0), host),
                () -> assertEquals(node.getChildren().get(0).getChildren().get(0), server));
    }

    @Test
    void exportTopology_ShouldReturnExportedString() {
        String expString = assertDoesNotThrow(() -> ttController.exportTopology(root));
        assertEquals(expString, str1);
    }

    private static Stream<String> loadTopologyArgFactory() {
        return Stream.of(str1, str2, str3, str4);
    }

    TTControllerImplTest() throws TTException {}
}
