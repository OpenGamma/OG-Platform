/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;

/**
 * Provides a convenient interface for requesting the engine to build a set of market data and use it to perform
 * some calculations.
 * <p>
 * This combines the separate operations on the {@link Engine} interface for building market data and running
 * calculations. It exists because it can have a sensible remote implementation, unlike {@code Engine} which
 * would require all the market data to be serialized and passed back and forth between the client and server.
 * <p>
 * This interface was created to retain existing functionality after refactoring of the engine, and in the
 * longer term might be replaced with a more general purpose calculation server. It is not guaranteed to be
 * supported indefinitely.
 */
public interface ViewRunner {

  /**
   * Creates a view and uses it to perform a set of calculations.
   *
   * @param calculationArguments options used when performing calculations
   * @param viewConfig configuration defining the view that will perform the calculations
   * @param suppliedData pre-built market data to be used in the calculations. If market data is required by the
   * functions that isn't supplied, the engine will attempt to build it.
   * @param portfolio the trades, securities (or anything else) that are the inputs to the calculations
   * @return the calculation results
   */
  Results runView(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment suppliedData,
      List<?> portfolio);

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   * <p>
   * The market data for each scenario is derived from the base market data by applying perturbations from
   * the scenario definition.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param calculationArguments arguments used when performing the calculations for the scenarios
   * @param baseMarketData the base market data used to derive the data for each scenario
   * @param scenarioDefinition defines how the base market data is perturbed to derive the data for each scenario
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   */
  ScenarioResults runScenarios(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment baseMarketData,
      ScenarioDefinition scenarioDefinition,
      List<?> portfolio);
}
