/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.sesame.Environment;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.marketdata.GatheringMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.function.Function;

/**
 * Runs individual functions in the same way they are run by the engine.
 * <p>
 * Functions are run in a three step process:
 * <ol>
 *   <li>Run function with no market data, record requests for data</li>
 *   <li>Build data that was requested by the functions but not supplied by the user</li>
 *   <li>Run function with market data and return the result</li>
 * </ol>
 * This is intended for use in tests that test individual functions.
 */
public class FunctionRunner {

  /** Builds market data in response to requirements gathered from the functions. */
  private final MarketDataEnvironmentFactory _environmentFactory;

  /**
   * @param environmentFactory builds market data that is required by functions but not provided by the user
   */
  public FunctionRunner(MarketDataEnvironmentFactory environmentFactory) {
    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "environmentFactory");
  }

  /**
   * Builds the required market data and uses it to run an individual function.
   * <p>
   * This is primarily intended for use in tests. It allows the engine to take care of building
   * the market data environment without requiring the caller to provide the configuration for an
   * entire view.
   *
   * @param calculationArguments options used when invoking the function
   * @param fn the function
   * @param <R> the return type of the function
   * @return the value returned by the function
   */
  public <R> R runFunction(CalculationArguments calculationArguments, Function<Environment, R> fn) {
    MarketDataEnvironment marketDataEnvironment =
        new MarketDataEnvironmentBuilder().valuationTime(calculationArguments.getValuationTime()).build();
    return runFunction(calculationArguments, marketDataEnvironment, fn);
  }

  /**
   * Builds the required market data and uses it to run an individual function.
   * <p>
   * This is primarily intended for use in tests. It allows the engine to take care of building
   * the market data environment without requiring the caller to provide the configuration for an
   * entire view.
   *
   * @param calculationArguments options used when invoking the function
   * @param suppliedData pre-built market data to be used in the calculations. If market data is required by the
   *   functions that isn't supplied, the engine will attempt to build it.
   * @param fn the function
   * @param <R> the return type of the function
   * @return the value returned by the function
   */
  public <R> R runFunction(CalculationArguments calculationArguments,
                           MarketDataEnvironment suppliedData,
                           Function<Environment, R> fn) {

    GatheringMarketDataBundle gatheringBundle = GatheringMarketDataBundle.create(suppliedData.toBundle());
    ZonedDateTime valuationTime = valuationTime(calculationArguments, suppliedData);
    GatheringMarketDataEnvironment gatheringEnvironment = new GatheringMarketDataEnvironment(gatheringBundle,
                                                                                             valuationTime);
    Environment env1 = new SimpleEnvironment(valuationTime,
                                             gatheringEnvironment.toBundle(),
                                             FilteredScenarioDefinition.EMPTY);
    // The purpose of the first run is gathering market data requirements and no market data is provided.
    // Therefore the results of the first run are likely to be all failures and are ignored
    fn.apply(env1);
    Set<MarketDataRequirement> requirements = gatheringBundle.getRequirements();
    MarketDataEnvironment populatedEnvironment =
        _environmentFactory.build(suppliedData,
                                  requirements,
                                  calculationArguments.getMarketDataSpecification(),
                                  valuationTime);
    MarketDataEnvironment mergedData = MarketDataEnvironmentBuilder.merge(suppliedData, populatedEnvironment);
    Environment env2 = new SimpleEnvironment(valuationTime,
                                             mergedData.toBundle(),
                                             FilteredScenarioDefinition.EMPTY);

    return fn.apply(env2);
  }

  private static ZonedDateTime valuationTime(CalculationArguments calcArgs, MarketDataEnvironment marketData) {
    if (calcArgs.getValuationTime() != null) {
      return calcArgs.getValuationTime();
    } else {
      return marketData.getValuationTime();
    }
  }
}
