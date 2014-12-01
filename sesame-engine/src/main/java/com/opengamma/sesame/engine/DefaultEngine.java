/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.sesame.config.EngineUtils;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.GatheringMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Default implementation of {@link Engine} which provides the main entry point to the OpenGamma calculation engine.
 */
public class DefaultEngine implements Engine {

  /** Builds market data in response to requirements gathered from the functions. */
  private final MarketDataEnvironmentFactory _environmentFactory;

  /** For creating views for performing calculations using configuration passed into the run methods. */
  private final ViewFactory _viewFactory;

  /**
   * @param viewFactory for creating views for performing calculations using configuration passed into the run methods
   * @param environmentFactory builds market data in response to requirements gathered from the functions
   */
  public DefaultEngine(ViewFactory viewFactory, MarketDataEnvironmentFactory environmentFactory) {
    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "bundleBuilder");
    _viewFactory = ArgumentChecker.notNull(viewFactory, "viewFactory");
  }

  @Override
  public Results runView(ViewConfig viewConfig,
                         final CalculationArguments calculationArguments,
                         MarketDataEnvironment suppliedData,
                         final List<?> portfolio) {

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    try {
      return runAsync(view, calculationArguments, suppliedData, portfolio).get();
    } catch (InterruptedException | ExecutionException e) {
      // this won't happen
      throw new OpenGammaRuntimeException("Failed to run view", e);
    }
  }

  @Override
  public ScenarioResults runScenarios(ViewConfig viewConfig,
                                      ScenarioCalculationArguments calculationArguments,
                                      ScenarioMarketDataEnvironment suppliedData,
                                      List<?> portfolio) {
    Map<String, MarketDataEnvironment> scenarioData = suppliedData.getData();

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    List<ListenableFuture<Pair<String, Results>>> resultFutures = new ArrayList<>(scenarioData.size());

    for (Map.Entry<String, MarketDataEnvironment> entry : scenarioData.entrySet()) {
      final String scenarioName = entry.getKey();
      MarketDataEnvironment scenarioMarketData = entry.getValue();
      CalculationArguments calcArgs = calculationArguments.argumentsForScenario(scenarioName);
      ListenableFuture<Results> future = view.runAsync(calcArgs, scenarioMarketData, portfolio);
      ListenableFuture<Pair<String, Results>> futureWithName =
          Futures.transform(future, new Function<Results, Pair<String, Results>>() {
        @Override
        public Pair<String, Results> apply(Results results) {
          return Pairs.of(scenarioName, results);
        }
      });
      resultFutures.add(futureWithName);
    }
    ListenableFuture<List<Pair<String, Results>>> combinedFuture = Futures.allAsList(resultFutures);
    List<Pair<String, Results>> scenarioNamesAndResults;
    try {
      scenarioNamesAndResults = combinedFuture.get();
      Map<String, Results> resultsMap = new HashMap<>(scenarioNamesAndResults.size());

      for (Pair<String, Results> scenarioNameAndResults : scenarioNamesAndResults) {
        String scenarioName = scenarioNameAndResults.getFirst();
        Results scenarioResults = scenarioNameAndResults.getSecond();
        resultsMap.put(scenarioName, scenarioResults);
      }
      return new ScenarioResults(resultsMap);
    } catch (InterruptedException | ExecutionException e) {
      // this won't happen
      throw new OpenGammaRuntimeException("Failed to run scenarios", e);
    }
  }

  private ListenableFuture<Results> runAsync(View view,
                                             CalculationArguments calculationArguments,
                                             MarketDataEnvironment suppliedData,
                                             List<?> inputs) {

    GatheringMarketDataBundle gatheringBundle = GatheringMarketDataBundle.create(suppliedData.toBundle());
    ZonedDateTime valuationTime = valuationTime(calculationArguments, suppliedData);
    view.run(calculationArguments, new GatheringMarketDataEnvironment(gatheringBundle, valuationTime), inputs);
    Set<MarketDataRequirement> requirements =
        Sets.<MarketDataRequirement>union(gatheringBundle.getRequirements(),
                                          gatheringBundle.getTimeSeriesRequirements());
    MarketDataEnvironment populatedEnvironment =
        _environmentFactory.build(suppliedData,
                                  requirements,
                                  calculationArguments.getMarketDataSpecification(),
                                  valuationTime);
    MarketDataEnvironment mergedData = MarketDataEnvironmentBuilder.merge(suppliedData, populatedEnvironment);
    return view.runAsync(calculationArguments, mergedData, inputs);
  }

  private static ZonedDateTime valuationTime(CalculationArguments calcArgs, MarketDataEnvironment marketData) {
    if (calcArgs.getValuationTime() != null) {
      return calcArgs.getValuationTime();
    } else {
      return marketData.getValuationTime();
    }
  }
}
