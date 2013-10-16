/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Stores job execution results for a calculation configuration.
 */
public class DependencyNodeJobExecutionResultCache {

  private final Map<ValueSpecification, DependencyNodeJobExecutionResult> _resultsBySpec = new ConcurrentHashMap<ValueSpecification, DependencyNodeJobExecutionResult>();

  public void put(final ValueSpecification valueSpec, DependencyNodeJobExecutionResult jobExecutionResult) {
    _resultsBySpec.put(valueSpec, jobExecutionResult);
  }

  /**
   * Stores the execution result for a dependency graph node.
   * 
   * @param node the dependency node to store, not null
   * @param jobExecutionResult the result to store, not null
   */
  public void put(final DependencyNode node, final DependencyNodeJobExecutionResult jobExecutionResult) {
    final int outputs = node.getOutputCount();
    for (int i = 0; i < outputs; i++) {
      put(node.getOutputValue(i), jobExecutionResult);
    }
  }

  public DependencyNodeJobExecutionResult get(final ValueSpecification valueSpec) {
    return _resultsBySpec.get(valueSpec);
  }

  /**
   * Finds the execution result for a dependency graph node.
   * 
   * @param node the dependency node to search for, not null
   * @return the execution result, null if no match
   */
  public DependencyNodeJobExecutionResult get(final DependencyNode node) {
    final int outputs = node.getOutputCount();
    for (int i = 0; i < outputs; i++) {
      final DependencyNodeJobExecutionResult result = get(node.getOutputValue(i));
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the set of data produced by nodes that have already been executed.
   * 
   * @return the executed values
   */
  public Set<ValueSpecification> getExecutedData() {
    return _resultsBySpec.keySet();
  }

}
