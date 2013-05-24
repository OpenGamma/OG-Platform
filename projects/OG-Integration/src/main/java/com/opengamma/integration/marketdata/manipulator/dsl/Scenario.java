/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
 *
 */
public class Scenario implements ScenarioBuilder {

  /** Default calculation configuration name. TODO does this exist as a constant somewhere else? */
  private static final String DEFAULT = "Default";

  private final Map<DistinctMarketDataSelector, FunctionParameters> _manipulations = Maps.newHashMap();

  public CurveSelector.Builder curve() {
    return new CurveSelector.Builder(this, DEFAULT);
  }

  public ScenarioBuilder calculationConfig(String configName) {
    return new CalculationConfigurationSelector(this, configName);
  }

  public Map<DistinctMarketDataSelector, FunctionParameters> getMarketDataManipulations() {
    return Collections.unmodifiableMap(_manipulations);
  }

  /* package */ void add(DistinctMarketDataSelector selector, StructureManipulator<?> manipulator) {
    SimpleFunctionParameters parameters = new SimpleFunctionParameters();
    parameters.setValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME, manipulator);
    _manipulations.put(selector, parameters);
  }
}
