package com.sap.hana.topology.tree.processor;

import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.util.CommonUtils;
import com.sap.hana.topology.util.FileUtils;
import com.sap.hana.topology.util.TTException;
import com.sap.hana.topology.util.TreeUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Processor for importing topology file from hdbnsutil */
@Processor(processorType = ProcessorType.IMPORT)
public final class TTNsutilImpProcessor implements TTProcessor<String, TTNode<String>> {
  /** Id for the processor, it's hardcoded base on the first line of the related file */
  private final String[] processorId = {FileUtils.HDBNSUTIL_TOPOLOGY_START_STRING};

  /**
   * Load topology tree from the output string of hdbnsutil
   *
   * @param topologyStr the output topology string of hdbnsutil
   * @return topology tree root node
   */
  @Override
  public TTNode<String> process(String topologyStr) throws TTProcessException {
    // split file to lines;

    // create root node
    TTNode<String> curNode = new TTNode<>(TreeUtils.TOPOLOGY_TREE_ROOT_NAME);

    String[] tops = FileUtils.getTopologyInfo(topologyStr);

    if (tops == null) {
      throw new TTProcessException("File format is not supported!");
    }

    try {
      // Reading line by line, skipping the first line
      for (int i = 1; i < tops.length; i++) {
        String line = tops[i];
        // skip empty line
        if (CommonUtils.isNullOrEmpty(line)) {
          continue;
        }

        // end of topology file
        if (line.substring(0, 2).equalsIgnoreCase("s<")) {
          break;
        }

        // set value
        if (line.substring(0, 2).equalsIgnoreCase("v>")) {
          curNode.setValue(line.substring(2));
          continue;
        }

        // get n + <lvl> + > part, eg: n3>
        Pattern pattern = Pattern.compile("n\\d+>");
        Matcher m = pattern.matcher(line);
        if (m.find()) {
          int lvl =
              Integer.parseInt(
                  line.substring(m.start() + 1, m.end() - 1)); // get level: n3> ->>> get 3
          String newNodeName = line.substring(m.end());

          if (lvl <= curNode.getLevel()) {
            curNode = TreeUtils.getParentByLevel(curNode, lvl);
          }
          TTNode<String> newNode = new TTNode<>(curNode, newNodeName);
          curNode.addChild(newNode);
          curNode = newNode;
        }
      }
    } catch (TTException e) {
      throw new TTProcessException(e.getMessage());
    }
    if (curNode.getRoot() == curNode) {
      // node is empty, something wrong with the format
      throw new TTProcessException("File format is not supported!");
    }
    return curNode.getRoot();
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
