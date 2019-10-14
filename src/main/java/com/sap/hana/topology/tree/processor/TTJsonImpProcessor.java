package com.sap.hana.topology.tree.processor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sap.hana.topology.exception.TTProcessException;
import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.util.FileUtils;

import java.util.Map;

/**
 * Processor for importing name server JSON topology file
 */
@Processor(processorType = ProcessorType.IMPORT)
public class TTJsonImpProcessor implements TTProcessor<String, TTNode> {

    /**
     * Load topology tree from json string
     * @param topologyStr the json string of topology tree
     * @return topology tree root node
     */
    @Override
    public TTNode process(String topologyStr) throws TTProcessException {
        final String ROOT_STRING = "topology"; //Should not use TopologyTreeUtils.TOPOLOGY_TREE_ROOT_NAME, because this just happens to be the same value.

        Map<String, Object> topologyMap;
        try {
            topologyMap = new Gson().fromJson(topologyStr, new TypeToken<Map<String, Object>>(){}.getType());
        } catch (JsonSyntaxException e) {
            throw new TTProcessException("File format is not supported, JSON parse error!");
        }

        if (topologyMap == null || !topologyMap.containsKey(ROOT_STRING)) {
            throw new TTProcessException("File format is not supported!");
        }

        TTNode root = new TTNode(ROOT_STRING);
        return buildTopologyTree(topologyMap.get(ROOT_STRING), root);
    }

    /**
     * Build Topology Tree base on Gson LinkedTreeMap
     * @param obj Gson LinkedTreeMap node (for a non-leaf node) or String/Long value (for a leaf node)
     * @param node Current Tree Node
     * @return Tree Node
     */
    @SuppressWarnings("unchecked")
    private TTNode buildTopologyTree(Object obj, TTNode node) {
        if (obj instanceof Map) {
            Map<String, Object> mapItem = (Map<String, Object>)obj;
            for (String key : mapItem.keySet()) {
                node.addChild(buildTopologyTree(mapItem.get(key), new TTNode(node, key)));
            }
        } else {
            if (obj instanceof Double) {
                //Gson uses "double" for those "long" numbers, change it bak because we do not have "double" in JSON file
                node.setValue(String.valueOf(((Double) obj).longValue()));
            } else {
                node.setValue(String.valueOf(obj));
            }
        }
        return node;
    }

    /**
     * Get build id
     * @return build id
     */
    @Override
    public String[] getProcessorId() {
        return new String[] { FileUtils.JSON_TOPOLOGY_START_STRING };
    }

}
