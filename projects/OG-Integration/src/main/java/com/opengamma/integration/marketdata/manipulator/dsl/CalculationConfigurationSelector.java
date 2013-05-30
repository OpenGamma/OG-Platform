/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CalculationConfigurationSelector implements ScenarioBuilder {

  private final String _calcConfigName;
  private final Scenario _scenario;

  public CalculationConfigurationSelector(Scenario scenario, String calcConfigName) {
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(scenario, "scenario");
    _scenario = scenario;
    _calcConfigName = calcConfigName;
  }

  @Override
  public CurveSelector.Builder curve() {
    return new CurveSelector.Builder(_scenario, _calcConfigName);
  }
}
