package com.sap.hana.topology.tree.controller;

import com.sap.hana.topology.exception.TTException;
import com.sap.hana.topology.tree.TTNode;

/**
 * Interface of topology tree controller
 */
public interface TTController {

    /**
     * Load topology from topology string to topology tree node.
     * @param topologyStr topology string
     * @return topology tree root node
     * @throws TTException topology tree exception
     */
    TTNode loadTopology(String topologyStr) throws TTException;

    /**
     * Export topology from topology tree node to topology string
     * @param topologyNode topology tree root node
     * @return topology string
     * @throws TTException topology tree exception
     */
    String exportTopology(TTNode topologyNode) throws TTException;
}
