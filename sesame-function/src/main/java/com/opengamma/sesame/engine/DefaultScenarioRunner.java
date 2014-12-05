/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link ScenarioRunner}.
 *
 * @deprecated use {@link Engine#runScenarios(ViewConfig, ScenarioCalculationArguments, ScenarioMarketDataEnvironment, List)}
 */
public class DefaultScenarioRunner implements ScenarioRunner {

  private final Engine _engine;

  /**
   * @param engine the engine which performs the calculations
   */
  public DefaultScenarioRunner(Engine engine) {
    _engine = ArgumentChecker.notNull(engine, "engine");
  }

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   * <p>
   * This only handles one very specific use case: views whose calculations require curves and no other market data.
   * The pre-calibrated multicurve bundles must be in the market data environment.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param marketDataEnvironment contains the market data for the scenarios
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   *
   * @deprecated use {@link Engine#runScenarios(ViewConfig, ScenarioCalculationArguments, ScenarioMarketDataEnvironment, List)}
   */
  @Deprecated
  @Override
  public ScenarioResults runScenario(ViewConfig viewConfig,
                                     ScenarioMarketDataEnvironment marketDataEnvironment,
                                     List<?> portfolio) {
    CalculationArguments calculationArguments = CalculationArguments.builder().build();
    ScenarioCalculationArguments scenarioArguments = ScenarioCalculationArguments.of(calculationArguments);
    return _engine.runScenarios(viewConfig, scenarioArguments, marketDataEnvironment, portfolio);
  }
}
