package com.sap.hana.topology.util;

import java.util.ArrayList;
import java.util.List;

/** Utilities for Topology File */
public final class FileUtils {

  /** Topology file which generated by hdbnsutil must be started with the "start string" */
  public static final String HDBNSUTIL_TOPOLOGY_START_STRING = "s>topology3";

  /** Topology file which generated by hdbnsutil should be ended with the "end string" */
  public static final String HDBNSUTIL_TOPOLOGY_END_STRING = "s<topology3";

  /** Topology file which generated in Full System Dump (HANA 1.0) should be started with "''=" */
  public static final String FSID_TOPOLOGY_START_STRING_V1 = "''=";

  /** Topology file which generated in Full System Dump (HANA 2.0) should be started with "''" */
  public static final String FSID_TOPOLOGY_START_STRING_V2 = "''";

  /** Specify how many space number equals 1 topology level in Full System Dump */
  public static final int FSID_1_LEVEL_SPACE_NUMBER = 2;

  /** Topology file which generated by name server should be started with "{" */
  public static final String JSON_TOPOLOGY_START_STRING = "{";

  /**
   * Get topology string arrays from topology string, skips the empty lines
   *
   * @param topologyStr topology string from topology file
   * @return topology array
   */
  public static String[] getTopologyInfo(String topologyStr) {
    if (CommonUtils.isNullOrEmpty(topologyStr)) {
      return null;
    }

    String[] tops = topologyStr.split("[\r\n]+");

    if (tops.length == 0) {
      return null;
    }

    List<String> topologyInfo = new ArrayList<>();

    for (String line : tops) {
      // skip all blank lines
      if (!CommonUtils.isNullOrEmpty(line) && !CommonUtils.isNullOrEmpty(line.trim())) {
        topologyInfo.add(line);
      }
    }

    return topologyInfo.toArray(new String[0]);
  }

  /**
   * Get first line from string that loaded from topology file
   *
   * @param topologyStr topology string
   * @return the first line of the topology string
   */
  public static String getFirstLineFromTopologyStr(String topologyStr) {
    String[] tops = getTopologyInfo(topologyStr);
    if (tops != null && tops.length > 0) {
      return tops[0];
    }

    return null;
  }
}
