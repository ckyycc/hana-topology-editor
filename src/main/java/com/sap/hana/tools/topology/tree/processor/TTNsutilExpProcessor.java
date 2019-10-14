package com.sap.hana.tools.topology.tree.processor;

import com.sap.hana.tools.topology.exception.TTProcessException;
import com.sap.hana.tools.topology.tree.TTNode;
import com.sap.hana.tools.topology.util.CommonUtils;
import com.sap.hana.tools.topology.util.FileUtils;
import com.sap.hana.tools.topology.util.TreeUtils;

/**
 * Processor for export topology file for hdbnsutil
 */
@Processor(processorType = ProcessorType.EXPORT)
public class TTNsutilExpProcessor implements TTProcessor<TTNode, String> {

    /**
     * Get the string for exporting from topology tree
     * @param topologyNode topology tree
     * @return string of topology tree
     */
    @Override
    public String process(TTNode topologyNode) throws TTProcessException {
        if (topologyNode == null) {
            throw new TTProcessException("Internal error occurred, the provided tree node is empty.");
        }
        return FileUtils.HDBNSUTIL_TOPOLOGY_START_STRING + "\n" + getStringFromTopologyTree(topologyNode) + "\n" + FileUtils.HDBNSUTIL_TOPOLOGY_END_STRING + "\n";
    }

    /**
     * Generate string from the topology tree
     * @param topologyNode node of the tree
     * @return string of the topology tree
     */
    private String getStringFromTopologyTree(TTNode topologyNode) {
        StringBuilder treeBuffer = new StringBuilder();
        treeBuffer.append(getStringFromTopologyNode(topologyNode));
        if (topologyNode.getChildren() != null) {
            for (TTNode item : topologyNode.getChildren()) {
                if (treeBuffer.length() > 0)
                    treeBuffer.append("\n");
                treeBuffer.append(getStringFromTopologyTree(item));
            }
        }

        return treeBuffer.toString();
    }

    /**
     * Get string from one topology tree node
     * @param topologyNode topology tree node
     * @return string of the provided topology tree node
     */
    private String getStringFromTopologyNode(TTNode topologyNode) {
        if (topologyNode.getLevel() <= 0) {
            return "";
        }
        String value = "v>" + topologyNode.getValue();
        String name = "n" + topologyNode.getLevel() + ">" + topologyNode.getName();

        return !CommonUtils.isNullOrEmpty(topologyNode.getValue()) ? name + "\n" + value : name;
    }

    /**
     * Get build id
     * @return build id
     */
    public String[] getProcessorId() {
        return new String[] { TreeUtils.TOPOLOGY_TREE_EXPORTER_ID };
    }

}
