package com.sap.hana.topology.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class CommonUtils {
  /** Check whether the given string is null or empty */
  public static boolean isNullOrEmpty(String str) {
    return str == null || str.length() == 0;
  }

  /** Check whether the given list is null or empty */
  public static boolean isNullOrEmpty(Collection<?> o) {
    return o == null || o.isEmpty();
  }

  /**
   * List all the classes in the specified package
   *
   * @param packageName the package name to search
   * @return classes list in the package
   */
  public static List<Class<?>> getClasses(String packageName)
      throws IOException, ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<>();
    if (isNullOrEmpty(packageName)) return classes;

    ClassLoader cld = Thread.currentThread().getContextClassLoader();

    Enumeration<URL> resources = cld.getResources(packageName.replace('.', '/'));
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      URLConnection connection = url.openConnection();

      if (connection instanceof JarURLConnection) {
        checkJarFile((JarURLConnection) connection, packageName, classes);
      } else {
        checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), packageName, classes);
      }
    }

    return classes;
  }

  /**
   * List all classes base on package name in directory (not in jar file)
   *
   * @param directory The directory to start with
   * @param packageName The package name
   * @param classes classes list in the package
   */
  private static void checkDirectory(File directory, String packageName, List<Class<?>> classes)
      throws ClassNotFoundException {
    if (directory.exists() && directory.isDirectory()) {
      String[] files = directory.list();
      if (files == null) {
        return;
      }
      for (String file : files) {
        if (file.endsWith(".class")) {
          classes.add(Class.forName(packageName + '.' + file.substring(0, file.length() - 6)));
        } else {
          File tmpDirectory = new File(directory, file);
          if (tmpDirectory.isDirectory()) {
            checkDirectory(tmpDirectory, packageName + "." + file, classes);
          }
        }
      }
    }
  }

  /**
   * List all classes base on package name in jar file
   *
   * @param connection the connection to the jar
   * @param packageName the package name
   * @param classes classes list in the package
   */
  private static void checkJarFile(
      JarURLConnection connection, String packageName, List<Class<?>> classes)
      throws ClassNotFoundException, IOException {
    JarFile jarFile = connection.getJarFile();
    Enumeration<JarEntry> entries = jarFile.entries();

    for (JarEntry jarEntry;
        entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null); ) {
      String name = jarEntry.getName();

      if (name.contains(".class")) {
        name = name.substring(0, name.length() - 6).replace('/', '.');
        if (name.contains(packageName)) {
          classes.add(Class.forName(name));
        }
      }
    }
  }
}
