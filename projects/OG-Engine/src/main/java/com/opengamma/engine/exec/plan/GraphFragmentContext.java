/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;

/**
 * State shared among fragments of a dependency graph as part of the multiple node partitioning algorithm.
 */
/*package*/class GraphFragmentContext {

  private final String _calculationConfig;
  private final ExecutionLogModeSource _logModeSource;
  private final long _functionInitializationId;
  private final Map<ValueSpecification, Boolean> _sharedCacheValues = new HashMap<ValueSpecification, Boolean>();
  private final Map<ValueSpecification, FunctionParameters> _parameters;

  public GraphFragmentContext(final String calculationConfig, final ExecutionLogModeSource logModeSource, final long functionInitializationId,
      final Collection<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
    _calculationConfig = calculationConfig;
    _logModeSource = logModeSource;
    _functionInitializationId = functionInitializationId;
    for (ValueSpecification sharedValue : sharedValues) {
      _sharedCacheValues.put(sharedValue, Boolean.TRUE);
    }
    _parameters = parameters;
  }

  public String getCalculationConfig() {
    return _calculationConfig;
  }

  public ExecutionLogModeSource getLogModeSource() {
    return _logModeSource;
  }

  public long getFunctionInitId() {
    return _functionInitializationId;
  }

  public Map<ValueSpecification, Boolean> getSharedCacheValues() {
    return _sharedCacheValues;
  }

  public void setTerminalOutputs(final Collection<ValueSpecification> outputs) {
    for (ValueSpecification output : outputs) {
      _sharedCacheValues.put(output, Boolean.TRUE);
    }
  }

  public Map<ValueSpecification, FunctionParameters> getParameters() {
    return _parameters;
  }

}
