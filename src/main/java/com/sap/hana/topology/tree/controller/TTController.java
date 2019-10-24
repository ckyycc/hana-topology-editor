package com.sap.hana.topology.tree.controller;

import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.util.TTException;

/** Interface of topology tree controller */
public interface TTController {

  /**
   * Load topology from topology string to topology tree node.
   *
   * @param topologyStr topology string
   * @return topology tree root node
   * @throws TTException topology tree exception
   */
  TTNode<String> loadTopology(String topologyStr) throws TTException;

  /**
   * Export topology from topology tree node to topology string
   *
   * @param topologyNode topology tree root node
   * @return topology string
   * @throws TTException topology tree exception
   */
  String exportTopology(TTNode<String> topologyNode) throws TTException;
}
