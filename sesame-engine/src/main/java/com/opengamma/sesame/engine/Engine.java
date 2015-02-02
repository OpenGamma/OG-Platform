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
 */
public interface Engine {

  /**
   * Creates a view and uses it to perform a set of calculations.
   * <p>
   * The valuation time in the market data is ignored, the valuation time from
   * {@code calculationArguments} is used. This will change in v3.0.
   *
   * @param calculationArguments options used when performing calculations
   * @param viewConfig configuration defining the view that will perform the calculations
   * @param marketData market data to be used in the calculations
   * @param portfolio the trades, securities (or anything else) that are the inputs to the calculations
   * @return the calculation results
   */
  Results runView(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment marketData,
      List<?> portfolio);

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   * <p>
   * The valuation time in the market data is ignored, the valuation time from {@code calculationArguments} is used.
   * This will change in v3.0.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param marketData the market data used in the calculations
   * @param calculationArguments options used when performing calculations
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   */
  ScenarioResults runScenarios(
      ViewConfig viewConfig,
      ScenarioMarketDataEnvironment marketData,
      CalculationArguments calculationArguments,
      List<?> portfolio);

  /**
   * Builds the market data required for performing calculations over a portfolio.
   * If the calculations require any data not provided in the {@code suppliedData} it is built by the
   * engine.
   * <p>
   * The valuation time in the supplied data is ignored, the valuation time from
   * {@code calculationArguments} is used. This will change in v3.0.
   *
   * @param suppliedData market data supplied by the caller
   * @param calculationArguments options used when performing calculations
   * @param portfolio the trades, securities (or anything else) that are the inputs to the calculations
   * @return the market data required by the calculations
   * */
  MarketDataEnvironment buildMarketData(
      ViewConfig viewConfig,
      MarketDataEnvironment suppliedData,
      CalculationArguments calculationArguments,
      List<?> portfolio);

  /**
   * Builds the market data required for performing calculations over a portfolio for a set of scenarios.
   * The scenario data is derived by applying the perturbations in the scenario definition to the base data.
   * <p>
   * The valuation time in the base data is ignored, the valuation time from
   * {@code calculationArguments} is used for all scenarios. This will change in v3.0.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param baseData the base market data used to derive the data for each scenario
   * @param scenarioDefinition defines how the market data for each scenario is derived from the base data
   * @param calculationArguments options used when performing calculations
   * @param portfolio the trades, securities (or anything else) that are the inputs to the calculations
   * @return the market data required by the calculations
   */
  ScenarioMarketDataEnvironment buildScenarioMarketData(
      ViewConfig viewConfig,
      MarketDataEnvironment baseData,
      ScenarioDefinition scenarioDefinition,
      CalculationArguments calculationArguments,
      List<?> portfolio);

  /**
   * Builds the market data required for performing calculations over a portfolio for a set of scenarios.
   * If the calculations require any data not provided in the {@code suppliedData} it is built by the
   * engine.
   * <p>
   * The valuation time in each individual {@link MarketDataEnvironment} is ignored, the valuation time from
   * {@code calculationArguments} is used for all scenarios. This will change in v3.0.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param suppliedData base market data for the scenarios
   * @param calculationArguments options used when performing calculations
   * @param portfolio the trades, securities (or anything else) that are the inputs to the calculations
   * @return the market data required by the calculations
   * @deprecated this is a temporary method to ease migration from 2.8 to 2.9
   */
  @Deprecated
  ScenarioMarketDataEnvironment buildScenarioMarketData(
      ViewConfig viewConfig,
      ScenarioMarketDataEnvironment suppliedData,
      CalculationArguments calculationArguments,
      List<?> portfolio);
}
