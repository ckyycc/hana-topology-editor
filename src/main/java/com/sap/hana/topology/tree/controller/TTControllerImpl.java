package com.sap.hana.topology.tree.controller;

import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.tree.processor.Processor;
import com.sap.hana.topology.tree.processor.ProcessorType;
import com.sap.hana.topology.tree.processor.TTProcessor;
import com.sap.hana.topology.util.CommonUtils;
import com.sap.hana.topology.util.FileUtils;
import com.sap.hana.topology.util.TTException;
import com.sap.hana.topology.util.TreeUtils;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/** Controller implementation, it's singleton. */
public final class TTControllerImpl implements TTController {

  /**
   * Load topology from topology string to topology tree node.
   *
   * @param topologyStr topology string
   * @return topology tree root node
   * @throws TTException topology tree exception
   */
  public TTNode<String> loadTopology(String topologyStr) throws TTException {
    final TTProcessor<String, TTNode<String>> processor =
        impProcessors.get(FileUtils.getFirstLineFromTopologyStr(topologyStr));

    if (processor == null) {
      throw new TTException("File format is not supported!");
    }

    return processor.process(topologyStr);
  }

  /**
   * Export topology from topology tree node to topology string
   *
   * @param topologyNode topology tree root node
   * @return topology string
   * @throws TTException topology tree exception
   */
  public String exportTopology(TTNode<String> topologyNode) throws TTException {
    final TTProcessor<TTNode<String>, String> processor =
        expProcessors.get(TreeUtils.TOPOLOGY_TREE_EXPORTER_ID);

    if (processor == null) {
      throw new TTException("Internal error occurred!");
    }

    return processor.process(topologyNode);
  }

  /** Singleton class, cannot be initialized directly. */
  private TTControllerImpl() {}

  /** Singleton */
  private static TTController instance;

  /** import processor map */
  private static final Map<String, TTProcessor<String, TTNode<String>>> impProcessors =
      new HashMap<>();

  /** export processor map */
  private static final Map<String, TTProcessor<TTNode<String>, String>> expProcessors =
      new HashMap<>();

  /**
   * Register import processor
   *
   * @param processor import processor
   */
  private static void registerImpProcessor(TTProcessor<String, TTNode<String>> processor) {
    for (String id : processor.getProcessorId()) {
      impProcessors.put(id, processor);
    }
  }

  /**
   * Register export processor
   *
   * @param processor export processor
   */
  private static void registerExpProcessor(TTProcessor<TTNode<String>, String> processor) {
    for (String id : processor.getProcessorId()) {
      expProcessors.put(id, processor);
    }
  }

  /** Register all the processors */
  private static void register() throws TTException {

    try {
      String packageName = Processor.class.getPackage().getName();

      for (Class<?> clazz : CommonUtils.getClasses(packageName)) {
        if (clazz.isAnnotationPresent(Processor.class)
            && TTProcessor.class.isAssignableFrom(clazz)
            && !Modifier.isAbstract(clazz.getModifiers())
            && !clazz.isInterface()) {
          Processor annotation = clazz.getAnnotation(Processor.class);
          if (annotation.processorType() == ProcessorType.IMPORT) {
            @SuppressWarnings("unchecked")
            Class<TTProcessor<String, TTNode<String>>> importProcessor =
                (Class<TTProcessor<String, TTNode<String>>>) clazz;
            registerImpProcessor(importProcessor.getConstructor().newInstance());
          } else if (annotation.processorType() == ProcessorType.EXPORT) {
            @SuppressWarnings("unchecked")
            Class<TTProcessor<TTNode<String>, String>> exportProcessor =
                (Class<TTProcessor<TTNode<String>, String>>) clazz;
            registerExpProcessor(exportProcessor.getConstructor().newInstance());
          } else {
            throw new TTException("Wrong processor type!");
          }
        }
      }
    } catch (InstantiationException
        | InvocationTargetException
        | NoSuchMethodException
        | IllegalAccessException
        | ClassNotFoundException
        | IOException e) {
      throw new TTException(e.getMessage());
    }
  }

  /**
   * Get instance, it is singleton
   *
   * @return the singleton instance
   */
  public static TTController getInstance() throws TTException {
    if (instance == null) {
      instance = new TTControllerImpl();
      register();
    }
    return instance;
  }
}
