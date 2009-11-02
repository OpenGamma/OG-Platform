/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author emcleod
 */
public class NamedVariableLeastSquaredRegressionResult extends LeastSquaresRegressionResult {
  private static final Logger s_Log = LoggerFactory.getLogger(NamedVariableLeastSquaredRegressionResult.class);
  private final List<String> _independentVariableNames;
  private final LeastSquaresRegressionResult _result;

  public NamedVariableLeastSquaredRegressionResult(final List<String> independentVariableNames, final LeastSquaresRegressionResult result) {
    super(result);
    if (independentVariableNames.size() != result.getBetas().length)
      throw new IllegalArgumentException("Length of variable name array did not match number of results in the regression");
    _independentVariableNames = independentVariableNames;
    _result = result;
  }

  /**
   * @return the _independentVariableNames
   */
  public List<String> getIndependentVariableNames() {
    return _independentVariableNames;
  }

  /**
   * @return the _result
   */
  public LeastSquaresRegressionResult getResult() {
    return _result;
  }

  public Double getPredictedValueForVariables(final Map<String, Double> namedVariables) {
    if (namedVariables == null)
      throw new IllegalArgumentException("Map was null");
    if (namedVariables.isEmpty()) {
      s_Log.warn("Map was empty: returning 0");
      return 0.;
    }
    final Double[] betas = getBetas();
    double sum = 0;
    if (namedVariables.size() > betas.length)
      throw new IllegalArgumentException("Number of named variables in map was greater than that in regression");
    int i = 0;
    for (final String name : getIndependentVariableNames()) {
      if (!namedVariables.containsKey(name))
        throw new IllegalArgumentException();
      sum += betas[i++] * namedVariables.get(name);
    }
    return sum;
  }
}
