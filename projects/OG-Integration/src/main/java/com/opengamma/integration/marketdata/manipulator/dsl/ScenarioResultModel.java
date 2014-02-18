/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Map;

import com.opengamma.util.ArgumentChecker;

/**
 * Results from running a scenario, includes the calculated results and the parameters used.
 */
public class ScenarioResultModel {

  private final SimpleResultModel _results;

  private final Map<String, Object> _parameters;

  /* package */ ScenarioResultModel(SimpleResultModel results, Map<String, Object> parameters) {
    _results = ArgumentChecker.notNull(results, "results");
    _parameters = ArgumentChecker.notNull(parameters, "parameters");
  }

  /**
   * @return The scenario name.
   */
  public String getScenarioName() {
    return _results.getCycleName();
  }

  /**
   * @return The calculated results.
   */
  public SimpleResultModel getResults() {
    return _results;
  }

  /**
   * @return The scenario parameters.
   */
  public Map<String, Object> getParameters() {
    return _parameters;
  }

  @Override
  public String toString() {
    return "ScenarioResultModel [" +
        ", _results=" + _results +
        "_parameters=" + _parameters +
        "]";
  }
}
