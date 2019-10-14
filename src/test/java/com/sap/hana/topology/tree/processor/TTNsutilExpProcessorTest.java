package com.sap.hana.topology.tree.processor;

import com.sap.hana.topology.exception.TTException;
import com.sap.hana.topology.exception.TTProcessException;
import com.sap.hana.topology.tree.TTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TTNsutilExpProcessorTest {

    private TTProcessor<TTNode, String> processor = new TTNsutilExpProcessor();

    private final static String str1 = "s>topology3\nn1>host\nn2>vanpghana11\ns<topology3\n";

    private final static TTNode root = new TTNode("topology");
    private final static TTNode host = new TTNode(root, "host");
    private final static TTNode server = new TTNode(host, "vanpghana11");

    @BeforeAll
    static void init() {
        root.addChild(host);
        host.addChild(server);
    }

    @Test
    void process_ShouldThrowExceptionIfNull() {
        TTException thrown = assertThrows(TTProcessException.class, () -> processor.process(null));
        assertTrue(thrown.getMessage().contains("Internal error occurred"));
    }

    @Test
    void process_ShouldReturnTreeNodeString() {
        String topologyTreeString = assertDoesNotThrow(() -> processor.process(root));
        assertEquals(topologyTreeString, str1);
    }
}
