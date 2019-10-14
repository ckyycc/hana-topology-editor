package com.sap.hana.tools.topology.tree.processor;

import com.sap.hana.tools.topology.exception.TTProcessException;
import com.sap.hana.tools.topology.exception.TTException;
import com.sap.hana.tools.topology.tree.TTNode;
import com.sap.hana.tools.topology.util.CommonUtils;
import com.sap.hana.tools.topology.util.FileUtils;
import com.sap.hana.tools.topology.util.TreeUtils;

/**
 * Processor for importing FSID topology txt file
 */
@Processor(processorType = ProcessorType.IMPORT)
public class TTFsidImpProcessor implements TTProcessor<String, TTNode> {
    /**
     * Load topology tree from full system dump topology file string
     * @param topologyStr the string of full system dump topology file
     * @return topology tree root node
     */
    @Override
    public TTNode process (String topologyStr) throws TTProcessException {
        // split file to lines;

        // create root node
        TTNode curNode = new TTNode(TreeUtils.TOPOLOGY_TREE_ROOT_NAME);
        String[] tops = FileUtils.getTopologyInfo(topologyStr);

        if (tops == null) {
            throw new TTProcessException("File format is not supported!");
        }

        try {
            // Reading line by line, skipping the first line
            for (int i = 1; i < tops.length; i++) {
                String line = tops[i];
                // skip empty line
                if (CommonUtils.isNullOrEmpty(line.trim())) {
                    continue;
                }

                //get value if have
                int level = getLevel(line);
                String[] node = line.substring(level * 2).split("=");
                TTNode newNode;

                if (level <= curNode.getLevel()) {
                    curNode = TreeUtils.getParentByLevel(curNode, level);
                }

                if (node.length > 1 && !CommonUtils.isNullOrEmpty(node[1].trim())) {
                    newNode = new TTNode(curNode, node[0], node[1]);
                } else {
                    newNode = new TTNode(curNode, node[0]);
                }

                curNode.addChild(newNode);
                curNode = newNode;
            }
        } catch (TTException e) {
            throw new TTProcessException(e.getMessage());
        }
        return curNode.getRoot();
    }

    /**
     * Get build id
     * @return build id
     */
    @Override
    public String[] getProcessorId() {
        return new String[] {FileUtils.FSID_TOPOLOGY_START_STRING_V1, FileUtils.FSID_TOPOLOGY_START_STRING_V2};
    }

    /**
     * Get Level from the leading space number of the string
     * @param s the node string
     * @return level of the node
     * @throws TTProcessException throws process exception if file format is not correct
     */
    private int getLevel(String s) throws TTProcessException {
        int level = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                level ++;
            } else {
                break;
            }
        }
        if (level % FileUtils.FSID_1_LEVEL_SPACE_NUMBER != 0) {
            throw new TTProcessException("File format is not supported!");
        }
        return level / FileUtils.FSID_1_LEVEL_SPACE_NUMBER;
    }
}
