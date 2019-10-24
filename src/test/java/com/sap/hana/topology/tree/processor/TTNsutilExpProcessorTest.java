package com.sap.hana.topology.tree.processor;

import static org.junit.jupiter.api.Assertions.*;

import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.util.TTException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TTNsutilExpProcessorTest {

  private TTProcessor<TTNode<String>, String> processor = new TTNsutilExpProcessor();

  private static final String str1 = "s>topology3\nn1>host\nn2>vanpghana11\ns<topology3\n";

  private static final TTNode<String> root = new TTNode<>("topology");
  private static final TTNode<String> host = new TTNode<>(root, "host");
  private static final TTNode<String> server = new TTNode<>(host, "vanpghana11");

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
