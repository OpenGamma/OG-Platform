/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;

/**
 * The main entry point to the OpenGamma calculation engine.
 * <p>
 * An engine receives requests from clients to perform calculations, selects the appropriate functions,
 * builds any required market data, executes the calculations and returns the results.
 * <p>
 * The caller can provide market data by populating and passing a {@link MarketDataEnvironment} when
 * making the request. The engine will attempt to create any market data that is required for the calculations
 * but not available in the data provided by the user.
 *
 * TODO the documentation needs to be expanded
 * TODO asynchronous versions of the run* methods?
 */
public interface Engine {

  /**
   * Creates a view and uses it to perform a set of calculations.
   *
   * @param calculationArguments options used when performing calculations
   * @param viewConfig configuration defining the view that will perform the calculations
   * @param suppliedData pre-built market data to be used in the calculations. If market data is required by the
   *   functions that isn't supplied, the engine will attempt to build it.
   * @param portfolio the trades, securities (or anything else) that are the inputs to the calculations
   * @return the calculation results
   */
  Results runView(ViewConfig viewConfig,
                  CalculationArguments calculationArguments,
                  MarketDataEnvironment suppliedData,
                  List<?> portfolio);

  // TODO should this take a list of measure names and rely on the default config? that's what the spec says
  //   or should there be another, similar method that takes measure names instead of a ViewConfig
  // TODO Using CalculationArguments means there is a single valuation time for all scenarios
  //   Is this a problem for now? Could use ScenarioCalculationArguments but would need to know the scenario names
  //   in advance. But currently the scenario names are generated in the engine.
  // Could use a source of CalculationArguments that is queries by scenario index

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
  ScenarioResults runScenarios(ViewConfig viewConfig,
                               CalculationArguments calculationArguments,
                               MarketDataEnvironment baseMarketData,
                               ScenarioDefinition scenarioDefinition,
                               List<?> portfolio);
}
