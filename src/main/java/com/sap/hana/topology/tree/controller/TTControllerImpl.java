package com.sap.hana.topology.tree.controller;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.sap.hana.topology.tree.processor.Processor;
import com.sap.hana.topology.util.TTException;
import com.sap.hana.topology.tree.processor.ProcessorType;
import com.sap.hana.topology.util.FileUtils;
import com.sap.hana.topology.util.TreeUtils;
import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.tree.processor.TTProcessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Class.forName;

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
    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    private static void register() throws TTException {

        try {
            //get class path
            ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
            //Get the package of processor
            String packageName = TTProcessor.class.getPackage().getName();
            //get all classes in the package
            ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClassesRecursive(packageName);
            //get all classes full name
            Collection<String> classNames = Collections2.transform(classes, classInfo -> classInfo != null ? classInfo.getName() : null);

            for (String name : classNames) {
                Class<?> clazz = forName(name);
                if (TTProcessor.class.isAssignableFrom(clazz) &&
                        !Modifier.isAbstract(clazz.getModifiers()) &&
                        !clazz.isInterface() &&
                        clazz.isAnnotationPresent(Processor.class)) {

                    Processor processorAnnotation = clazz.getAnnotation(Processor.class);
                    if (processorAnnotation.processorType() == ProcessorType.IMPORT) {
//                      Class<? extends TTProcessor<String, TopologyTreeNode>> c = Class.forName(name).asSubclass(TTFsidImpProcessor.class);
//                      registerImpProcessor(c.getConstructor().newInstance());
                        Class<TTProcessor<String, TTNode>> importProcessor = (Class<TTProcessor<String, TTNode>>) clazz;
                        registerImpProcessor(importProcessor.getConstructor().newInstance());

                    } else if (processorAnnotation.processorType() == ProcessorType.EXPORT) {
                        Class<TTProcessor<TTNode, String>> exportProcessor = (Class<TTProcessor<TTNode, String>>) clazz;
                        registerExpProcessor(exportProcessor.getConstructor().newInstance());

                    } else {
                        throw new TTException("Wrong processor type!");
                    }
                }
            }
        } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
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
