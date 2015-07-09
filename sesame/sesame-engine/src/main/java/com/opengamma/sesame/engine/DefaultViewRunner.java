/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * View runner that delegates to an {@link Engine} to build the market data and perform the calculations.
 */
public class DefaultViewRunner implements ViewRunner {

  /** The engine that builds the market data and performs the calculations. */
  private final Engine _engine;

  /**
   * @param engine engine that builds the market data and performs the calculations
   */
  public DefaultViewRunner(Engine engine) {
    _engine = ArgumentChecker.notNull(engine, "engine");
  }

  @Override
  public Results runView(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment suppliedData,
      List<?> portfolio) {

    MarketDataEnvironment marketData =
        _engine.buildMarketData(
            viewConfig,
            suppliedData,
            calculationArguments,
            portfolio);
    return _engine.runView(viewConfig, calculationArguments, marketData, portfolio);
  }

  @Override
  public ScenarioResults runScenarios(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment baseMarketData,
      ScenarioDefinition scenarioDefinition,
      List<?> portfolio) {

    ScenarioMarketDataEnvironment marketData =
        _engine.buildScenarioMarketData(
            viewConfig,
            baseMarketData,
            scenarioDefinition,
            calculationArguments,
            portfolio);
    return _engine.runScenarios(viewConfig, marketData, calculationArguments, portfolio);
  }
}
