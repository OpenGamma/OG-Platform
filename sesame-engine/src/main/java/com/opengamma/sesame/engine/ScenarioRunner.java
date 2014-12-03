/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;

/**
 * Runs the calculations in a view multiple times, once for each scenario defined in a set of market data.
 *
 * @deprecated use {@link Engine#runScenarios(ViewConfig, ScenarioCalculationArguments, ScenarioMarketDataEnvironment, List)}
 */
@Deprecated
public interface ScenarioRunner {

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   * <p>
   * If the view configuration contains {@code m} columns and the market data environment contains {@code n}
   * scenarios, the results will contain {@code m x n} values for each item in the portfolio.
   * <p>
   * N.B. this interface is likely to change. It will be necessary to support a different valuation time for
   * different scenarios so the {@code valuationTime} parameter will probably be absorbed into another object,
   * either the market data environment or a new class containing execution options for each scenario.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param marketDataEnvironment contains the market data for the scenarios
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   *
   * @deprecated use {@link Engine#runScenarios(ViewConfig, ScenarioCalculationArguments, ScenarioMarketDataEnvironment, List)}
   */
  @Deprecated
  ScenarioResults runScenario(ViewConfig viewConfig,
                              ScenarioMarketDataEnvironment marketDataEnvironment,
                              List<?> portfolio);
}
