package com.sap.hana.topology.tree.processor;

/**
 * Processor interface for topology tree
 *
 * @param <T> parameter
 * @param <V> return value
 */
public interface TTProcessor<T, V> {
  /**
   * process method for the processor
   *
   * @param parameter parameter for processor
   * @return returns of the "process"
   * @throws TTProcessException exception throws by the processor during "process"
   */
  V process(T parameter) throws TTProcessException;

  /**
   * Get processor ids (one processor may have multiple ids)
   *
   * @return processor ids
   */
  String[] getProcessorId();
}
