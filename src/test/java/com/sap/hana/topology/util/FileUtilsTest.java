package com.sap.hana.topology.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
    private static final String line1 = "TEST_LINE1", line2 = "TEST_LINE2";

    @ParameterizedTest
    @MethodSource("argFactoryNotEmpty")
    void getTopologyInfo_shouldSkipAllBlankLines(String topologyStr) {
        String[] result = FileUtils.getTopologyInfo(topologyStr);
        assertArrayEquals(result, new String[]{line1, line2});
    }

    @ParameterizedTest
    @MethodSource("argFactoryEmpty")
    void getTopologyInfo_shouldReturnNullIfEmpty(String topologyStr) {
        String[] result = FileUtils.getTopologyInfo(topologyStr);
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("argFactoryEmpty")
    void getFirstLineFromTopologyStr_shouldReturnNullIfEmpty(String topologyStr) {
        String result = FileUtils.getFirstLineFromTopologyStr(topologyStr);
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("argFactoryNotEmpty")
    void getFirstLineFromTopologyStr_shouldReturnFirstLineIfNotEmpty(String topologyStr) {
        String result = FileUtils.getFirstLineFromTopologyStr(topologyStr);
        assertEquals(result, line1);
    }

    private static Stream<String> argFactoryEmpty() {
        //null
        String t1 = null;
        //empty
        String t2 = "";
        //empty lines
        String t3 = "\n\n\n";
        return Stream.of(t1, t2, t3);
    }

    private static Stream<String> argFactoryNotEmpty() {
        //no blank line
        String t1 = line1 + "\n" + line2;
        //leading blank lines
        String t2 = " \n \n \n" + line1 + "\n" + line2;
        //trailing blank lines
        String t3 = line1 + "\n" + line2 + "\n \n \n ";
        //blank lines in the middle
        String t4 = line1 + "\n \n \n" + line2;
        //leading and middle blank lines
        String t5 = " \n \n \n" + line1 + "\n \n \n" + line2;
        //trailing and middle blank lines
        String t6 = line1 + "\n \n \n" + line2 + "\n \n \n";
        //leading and trailing blank lines
        String t7 = " \n \n \n" + line1 + "\n" + line2 + "\n \n \n";
        //leading, trailing and middle blank lines
        String t8 = " \n \n \n" + line1 + "\n \n \n" + line2 + "\n \n \n";
        return Stream.of(t1, t2, t3, t4, t5, t6, t7, t8);
    }
}
