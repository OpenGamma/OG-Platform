/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Function statistics for a single configuration.
 */
public final class FunctionCostsPerConfiguration {
  // the main functionality is in FunctionCosts

  /**
   * The parent function costs.
   */
  private final FunctionCosts _functionCosts;
  /**
   * The configuration name.
   */
  private final String _configurationName;
  /**
   * The map of per function statistics.
   */
  private final ConcurrentMap<String, FunctionInvocationStatistics> _data = new ConcurrentHashMap<String, FunctionInvocationStatistics>();

  /**
   * Creates an instance for a configuration name.
   * 
   * @param functionCosts  the function costs
   * @param configurationName  the configuration name, not null
   */
  FunctionCostsPerConfiguration(final FunctionCosts functionCosts, final String configurationName) {
    _functionCosts = functionCosts;
    _configurationName = configurationName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configuration name.
   * 
   * @return the configuration name, not null
   */
  public String getConfigurationName() {
    return _configurationName;
  }

  /**
   * Gets the costs.
   * 
   * @return the costs, not null
   */
  /* package */ ConcurrentMap<String, FunctionInvocationStatistics> getCosts() {
    return _data;
  }

  /**
   * Gets the statistics for a function.
   * 
   * @param functionId  the function id, not null
   * @return the statistics, not null
   */
  public FunctionInvocationStatistics getStatistics(final String functionId) {
    FunctionInvocationStatistics stats = _data.get(functionId);
    if (stats == null) {
      stats = _functionCosts.loadStatistics(this, functionId);
    }
    return stats;
  }

  /**
   * Gets the set of known functions.
   * 
   * @return the functions, not null
   */
  public Set<String> getFunctions() {
    return Collections.unmodifiableSet(_data.keySet());
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "FunctionCosts[" + _configurationName + ']';
  }

}
