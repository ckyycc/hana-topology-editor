package com.sap.hana.topology.util;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sap.hana.topology.tree.controller.TTController;
import com.sap.hana.topology.tree.controller.TTControllerImpl;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CommonUtilsTest {

  @Test
  void getClasses_ShouldReturnClassesListWhenClassNotInJarFile() {
    List<Class<?>> classList =
        assertDoesNotThrow(() -> CommonUtils.getClasses(TTController.class.getPackage().getName()));
    //        List<String> classNameList =
    // classList.stream().map(Class::getName).collect(Collectors.toList());
    assertAll(
        () -> assertTrue(classList.contains(TTController.class)),
        () -> assertTrue(classList.contains(TTControllerImpl.class)));
  }

  @Test
  void getClasses_ShouldReturnClassesListWhenClassInJarFile() {
    List<Class<?>> classList =
        assertDoesNotThrow(() -> CommonUtils.getClasses(JsonReader.class.getPackage().getName()));
    assertAll(
        () -> assertTrue(classList.contains(JsonReader.class)),
        () -> assertTrue(classList.contains(JsonToken.class)),
        () -> assertTrue(classList.contains(JsonWriter.class)));
  }

  @ParameterizedTest
  @MethodSource("argFactoryEmptyOrNotExist")
  void getClass_ShouldReturnSize0ListIfPackageIsEmptyOrDoesNotExist(String packageName) {
    List<Class<?>> classList = assertDoesNotThrow(() -> CommonUtils.getClasses(packageName));
    assertEquals(0, classList.size());
  }

  private static Stream<String> argFactoryEmptyOrNotExist() {
    // null
    String t1 = null;
    // empty
    String t2 = "";
    // empty lines
    String t3 = "com.sap.test.not.exist";
    return Stream.of(t1, t2, t3);
  }
}
