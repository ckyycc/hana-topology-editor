package com.sap.hana.topology.tree.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.util.TTException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TTJsonImpProcessorTest {
  private TTProcessor<String, TTNode<String>> processor = new TTJsonImpProcessor();

  private static final String str1 = "s>topology3\nn1>host\nn2>vanpghana11\ns<topology3\n";
  private static final String str2 = "{\n\"topology\":{\n\"host\":{\"vanpghana11\":{}}}}";
  private static final String str3 = "''=\n  host=\n    vanpghana11";
  private static final String str4 = "''\n  host\n    vanpghana11";

  private static final TTNode<String> root = new TTNode<>("topology");
  private static final TTNode<String> host = new TTNode<>(root, "host");
  private static final TTNode<String> server = new TTNode<>(host, "vanpghana11");

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
    TTNode<String> node = assertDoesNotThrow(() -> processor.process(topologyStr));
    assertAll(
        () -> assertEquals(node, root),
        () -> assertEquals(node.getChildren().get(0), host),
        () -> assertEquals(node.getChildren().get(0).getChildren().get(0), server));
  }

  @ParameterizedTest
  @MethodSource("loadTopologyArgFactoryWrongFormat")
  void process_shouldThrowExceptionIfFormatIsWrong(String topologyStr) {
    TTException thrown =
        assertThrows(TTProcessException.class, () -> processor.process(topologyStr));
    assertTrue(thrown.getMessage().contains("File format is not supported"));
  }

  private static Stream<String> loadTopologyArgFactoryCorrectFormat() {
    return Stream.of(str2);
  }

  private static Stream<String> loadTopologyArgFactoryWrongFormat() {
    return Stream.of(str1, str3, str4);
  }

  private static Stream<String> argFactoryEmpty() {
    // null
    String t1 = null;
    // empty
    String t2 = "";
    // empty lines
    String t3 = "\n\n\n";
    return Stream.of(t1, t2, t3);
  }
}
