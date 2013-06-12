/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Provdes simulation data for testing with the OG-Examples server, a few currencuy pairs and scaling factors to
 * apply to them.
 */
public class TestSimulationProvider implements SimulationSupplier {

  private static final Set<String> CURRENCY_PAIRS = ImmutableSet.of("GBPUSD", "EURUSD", "USDJPY", "CHFUSD");
  private static final List<Double> SCALING_FACTORS = ImmutableList.of(0.9, 0.95, 1.0, 1.05, 1.1);

  @Override
  public Simulation get() {
    Simulation.Builder simulation = Simulation.builder();
    for (Double scalingFactor : SCALING_FACTORS) {
      Scenario scenario = simulation.addScenario();
      for (String currencyPair : CURRENCY_PAIRS) {
        scenario.marketDataPoint().id("OG_SYNTHETIC_TICKER", currencyPair).apply().scaling(scalingFactor).execute();
      }
    }
    return simulation.build();
  }
}
