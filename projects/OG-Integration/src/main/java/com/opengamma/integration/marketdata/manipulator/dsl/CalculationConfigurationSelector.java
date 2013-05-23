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

  public CalculationConfigurationSelector(String calcConfigName) {
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    _calcConfigName = calcConfigName;
  }

  @Override
  public CurveSelector curve() {
    return new CurveSelector(_calcConfigName);
  }

  @Override
  public CalculationConfigurationSelector calculationConfig(String configName) {
    // TODO implement calculationConfig()
    throw new UnsupportedOperationException("calculationConfig not implemented");
  }
}
