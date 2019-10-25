package com.sap.hana.topology.tree.processor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.util.FileUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Processor for importing name server JSON topology file */
@Processor(processorType = ProcessorType.IMPORT)
public final class TTJsonImpProcessor implements TTProcessor<String, TTNode<String>> {

  /** Id for the processor, it's hardcoded base on the first line of the related file */
  private final String[] processorId = {FileUtils.JSON_TOPOLOGY_START_STRING};

  /**
   * Load topology tree from json string
   *
   * @param topologyStr the json string of topology tree
   * @return topology tree root node
   */
  @Override
  @SuppressWarnings("unchecked")
  public TTNode<String> process(String topologyStr) throws TTProcessException {
    // this is the root name in topology json file,
    // Should not use TopologyTreeUtils.TOPOLOGY_TREE_ROOT_NAME, because this just happens to be the
    // same value.
    final String ROOT_STRING = "topology";

    Map<String, Object> topologyMap;
    try {
      Type tokenType = new TypeToken<Map<String, Object>>() {}.getType();
      topologyMap =
          new GsonBuilder()
              .registerTypeAdapter(
                  tokenType,
                  (JsonDeserializer<Map<String, Object>>) // customizing the read logic
                      (json, typeOfT, context) -> (Map<String, Object>) read(json))
              .create()
              .fromJson(topologyStr, tokenType);

    } catch (JsonSyntaxException e) {
      throw new TTProcessException("File format is not supported, JSON parse error!");
    }

    if (topologyMap == null || !topologyMap.containsKey(ROOT_STRING)) {
      throw new TTProcessException("File format is not supported!");
    }

    return buildTopologyTree(topologyMap.get(ROOT_STRING), new TTNode<>(ROOT_STRING));
  }

  /**
   * All the "long" numbers will be parsed as double, which is wrong for this topology tree, because
   * the topology tree node does not have any Doubles. This function overwrites the original read
   * logic, all string, number and boolean will be treated as string
   *
   * @param in JsonElement
   * @return parsed object, should be list, map or string
   */
  private Object read(JsonElement in) {
    if (in.isJsonArray()) {
      // logic for array
      List<Object> list = new ArrayList<>();
      for (JsonElement element : in.getAsJsonArray()) {
        list.add(read(element));
      }
      return list;
    } else if (in.isJsonObject()) {
      // logic for map
      Map<String, Object> map = new LinkedTreeMap<>();
      for (Map.Entry<String, JsonElement> entry : in.getAsJsonObject().entrySet()) {
        map.put(entry.getKey(), read(entry.getValue()));
      }
      return map;
    } else if (in.isJsonPrimitive()) {
      // logic for primitive type, including string, number and boolean.
      // The string, number, boolean will all be treated as string.
      return in.getAsJsonPrimitive().getAsString();
    }
    return null;
  }

  /**
   * Build Topology Tree base on Gson LinkedTreeMap
   *
   * @param obj Gson LinkedTreeMap node (for a non-leaf node) or String/Long value (for a leaf node)
   * @param node Current Tree Node
   * @return Tree Node
   */
  @SuppressWarnings("unchecked")
  private TTNode<String> buildTopologyTree(Object obj, TTNode<String> node) {
    if (obj instanceof Map) {
      Map<String, Object> mapItem = (Map<String, Object>) obj;
      for (String key : mapItem.keySet()) {
        node.addChild(buildTopologyTree(mapItem.get(key), new TTNode<>(node, key)));
      }
    } else if (obj instanceof String) {
      node.setValue((String) obj);
    } else {
      node.setValue(String.valueOf(obj));
    }
    return node;
  }

  /**
   * Get processor id
   *
   * @return processor id
   */
  @Override
  public String[] getProcessorId() {
    return processorId.clone();
  }
}
