package com.sap.hana.tools.topology.tree.processor;

import com.sap.hana.tools.topology.exception.TTException;
import com.sap.hana.tools.topology.exception.TTProcessException;
import com.sap.hana.tools.topology.tree.TTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TTFsidImpProcessorTest {
    private TTProcessor<String, TTNode> processor = new TTFsidImpProcessor();

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

    @ParameterizedTest
    @MethodSource("argFactoryEmpty")
    void process_ShouldThrowExceptionIfEmpty() {
        TTException thrown = assertThrows(TTProcessException.class, () -> processor.process(""));
        assertTrue(thrown.getMessage().contains("File format is not supported"));
    }

    @ParameterizedTest
    @MethodSource("loadTopologyArgFactoryCorrectFormat")
    void process_ShouldReturnTreeNodeIfFormatIsCorrect(String topologyStr) {
        TTNode node = assertDoesNotThrow(() -> processor.process(topologyStr));
        assertAll(() -> assertEquals(node, root),
                () -> assertEquals(node.getChildren().get(0), host),
                () -> assertEquals(node.getChildren().get(0).getChildren().get(0), server));
    }

    @ParameterizedTest
    @MethodSource("loadTopologyArgFactoryWrongFormat")
    void process_shouldThrowExceptionIfFormatIsWrong(String topologyStr) {
        TTException thrown = assertThrows(TTProcessException.class, () -> processor.process(topologyStr));
        assertTrue(thrown.getMessage().contains("Can not get the parent"));
    }

    private static Stream<String> loadTopologyArgFactoryCorrectFormat() {
        return Stream.of(str3, str4);
    }
    private static Stream<String> loadTopologyArgFactoryWrongFormat() {
        return Stream.of(str1, str2);
    }
    private static Stream<String> argFactoryEmpty() {
        //null
        String t1 = null;
        //empty
        String t2 = "";
        //empty lines
        String t3 = "\n\n\n";
        return Stream.of(t1, t2, t3);
    }
}
