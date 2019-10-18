# Topology Editor for SAP HANA

HANA Topology Editor is a tool for viewing/editing HANA topology file written in Java.

&ensp;<img src="https://github.com/ckyycc/HANATopologyEditor/blob/master/src/demo/small-demo.gif" width="300">

It supports three different types of topology files: 
1. Exported via `hdbnsutil -exportTopology <topology file>`;
2. `topology.txt` in root directory of full system dump (System DB if MDC);
3. `nameserver_topology_<host>.json` in trace folder (HANA revision >= SPS03).
  
This tool is only designed for below scenarios:
1. View the topology info from the file exported via `hdbnsutil -exportTopology <topology file>`;
2. View the topology info from `topology.txt` in root directory of full system dump;
3. View `nameserver_topology_<host>.json` in trace folder;
4. Edit/Export the topology and import it back via `hdbnsutil -importTopology <topology file>` when HANA can't be started up because of the topology issues.

## Disclaimer:

Any usage of this HANA Topology Editor assumes that you have understood and agreed that:

1. HANA Topology Editor is NOT SAP official software, so normal SAP support of it cannot be assumed;
2. HANA Topology Editor is open source;
3. HANA Topology Editor is provided "as is";
4. HANA Topology Editor is to be used on "your own risk", please always backup the topology file before using it;
5. HANA Topology Editor is a one-man's hobby; developed, maintained and supported only during non-working hours.

## Download

This tool is written in Java 8, please make sure JRE version 1.8 (or later) is installed.

Download the [exe file (HANATopologyEditor.exe)](https://github.com/ckyycc/hana-topology-editor/raw/master/HANATopologyEditor.exe).

Download the [executable jar file (HANATopologyEditor.jar)](https://github.com/ckyycc/hana-topology-editor/raw/master/HANATopologyEditor.jar).



## Todo
* View HANA topology online
* ~~Edit HANA topology online (too dangerous)~~
* ...
