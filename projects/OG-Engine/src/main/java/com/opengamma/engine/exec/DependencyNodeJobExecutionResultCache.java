/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Stores job execution results for a calculation configuration.
 */
public class DependencyNodeJobExecutionResultCache {

  private final Map<ValueSpecification, DependencyNodeJobExecutionResult> _resultsBySpec = new ConcurrentHashMap<ValueSpecification, DependencyNodeJobExecutionResult>();
  
  public void put(ValueSpecification valueSpec, DependencyNodeJobExecutionResult jobExecutionResult) {
    _resultsBySpec.put(valueSpec, jobExecutionResult);
  }
  
  public DependencyNodeJobExecutionResult get(ValueSpecification valueSpec) {
    return _resultsBySpec.get(valueSpec);
  }
  
  /**
   * Finds the execution result for a job producing one or more of the given value specifications.
   * <p>
   * The first matching result is returned.
   * 
   * @param valueSpecs  the value specifications, not null
   * @return the execution result, null if no match
   */
  public DependencyNodeJobExecutionResult find(Set<ValueSpecification> valueSpecs) {
    for (ValueSpecification valueSpec : valueSpecs) {
      DependencyNodeJobExecutionResult result = _resultsBySpec.get(valueSpec);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
  
}
