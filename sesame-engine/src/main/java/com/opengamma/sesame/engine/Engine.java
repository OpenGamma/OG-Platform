/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;

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

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param calculationArguments arguments used when performing the calculations for the scenarios
   * @param marketDataEnvironment contains the market data for the scenarios
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   */
  ScenarioResults runScenarios(ViewConfig viewConfig,
                               ScenarioCalculationArguments calculationArguments,
                               ScenarioMarketDataEnvironment marketDataEnvironment,
                               List<?> portfolio);

  // TODO need a different runScenarios method for the market risk case
  //   * takes a scenario definition, not a view config. definition includes measure names
  //   * takes a single set of data (MarketDataEnvironment) instead of multiple (ScenarioMarketDataEnvironment)
}
