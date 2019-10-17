package com.sap.hana.topology.tree.controller;

import com.sap.hana.topology.tree.processor.Processor;
import com.sap.hana.topology.util.TTException;
import com.sap.hana.topology.tree.processor.ProcessorType;
import com.sap.hana.topology.util.FileUtils;
import com.sap.hana.topology.util.TreeUtils;
import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.tree.processor.TTProcessor;
import io.github.classgraph.ScanResult;
import io.github.classgraph.ClassGraph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller implementation, it's singleton.
 */
public class TTControllerImpl implements TTController {

    /**
     * Load topology from topology string to topology tree node.
     *
     * @param topologyStr topology string
     * @return topology tree root node
     * @throws TTException topology tree exception
     */
    public TTNode loadTopology(String topologyStr) throws TTException {
        TTProcessor<String, TTNode> processor = impProcessors.get(FileUtils.getFirstLineFromTopologyStr(topologyStr));

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
    public String exportTopology(TTNode topologyNode) throws TTException {
        TTProcessor<TTNode, String> processor = expProcessors.get(TreeUtils.TOPOLOGY_TREE_EXPORTER_ID);

        if (processor == null) {
            throw new TTException("Internal error occurred!");
        }

        return processor.process(topologyNode);
    }

    /**
     * Singleton class, cannot be initialized directly.
     */
    private TTControllerImpl() {

    }

    /**
     * Singleton
     */
    private static TTController instance;

    /**
     * import processor map
     */
    private static Map<String, TTProcessor<String, TTNode>> impProcessors = new HashMap<>();

    /**
     * export processor map
     */
    private static Map<String, TTProcessor<TTNode, String>> expProcessors = new HashMap<>();

    /**
     * Register import processor
     *
     * @param processor import processor
     */
    private static void registerImpProcessor(TTProcessor<String, TTNode> processor) {
        for (String id : processor.getProcessorId()) {
            impProcessors.put(id, processor);
        }
    }

    /**
     * Register export processor
     *
     * @param processor export processor
     */
    private static void registerExpProcessor(TTProcessor<TTNode, String> processor) {
        for (String id : processor.getProcessorId()) {
            expProcessors.put(id, processor);
        }
    }

    /**
     * Register all the processors
     */
    private static void register() throws TTException {

        try {
            String packageName = Processor.class.getPackage().getName();

            try (ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages(packageName).scan()) {
                List<Class<?>> processorClasses = scanResult.getClassesWithAnnotation(Processor.class.getName()).loadClasses();
                for (Class<?> clazz : processorClasses) {
                    if (TTProcessor.class.isAssignableFrom(clazz) &&
                            !Modifier.isAbstract(clazz.getModifiers()) &&
                            !clazz.isInterface()) {
                        Processor processorAnnotation = clazz.getAnnotation(Processor.class);
                        if (processorAnnotation.processorType() == ProcessorType.IMPORT) {
//                      Class<? extends TTProcessor<String, TopologyTreeNode>> c = Class.forName(name).asSubclass(TTFsidImpProcessor.class);
                            @SuppressWarnings("unchecked")
                            Class<TTProcessor<String, TTNode>> importProcessor = (Class<TTProcessor<String, TTNode>>) clazz;
                            registerImpProcessor(importProcessor.getConstructor().newInstance());
                        } else if (processorAnnotation.processorType() == ProcessorType.EXPORT) {
                            @SuppressWarnings("unchecked")
                            Class<TTProcessor<TTNode, String>> exportProcessor = (Class<TTProcessor<TTNode, String>>) clazz;
                            registerExpProcessor(exportProcessor.getConstructor().newInstance());
                        } else {
                            throw new TTException("Wrong processor type!");
                        }
                    }
                }
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
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
