/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.sesame.config.EngineUtils;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.GatheringMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.sesame.marketdata.scenarios.SinglePerturbationMapping;
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
                         CalculationArguments calculationArguments,
                         MarketDataEnvironment suppliedData,
                         List<?> portfolio) {

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    try {
      List<SinglePerturbationMapping> perturbations = ImmutableList.of();
      return runAsync(view, calculationArguments, suppliedData, perturbations, portfolio).get();
    } catch (InterruptedException | ExecutionException e) {
      // this won't happen unless there's a bug in the engine or a function interrupts its thread, which it shouldn't
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
      String scenarioName = entry.getKey();
      MarketDataEnvironment scenarioMarketData = entry.getValue();
      CalculationArguments calcArgs = calculationArguments.argumentsForScenario(scenarioName);
      // the future represents the results for a single scenario, which might not have finished calculating yet
      // the futures are executed asynchronously using the view's thread pool
      List<SinglePerturbationMapping> perturbations = ImmutableList.of();
      ListenableFuture<Results> future = runAsync(view, calcArgs, scenarioMarketData, perturbations, portfolio);
      resultFutures.add(futureWithScenarioName(future, scenarioName));
    }
    try {
      return scenarioResultsFuture(resultFutures).get();
    } catch (InterruptedException | ExecutionException e) {
      // this will only happen if there's a bug in the engine, all exceptions should be caught and converted to results
      throw new OpenGammaRuntimeException("Failed to run scenarios", e);
    }
  }

  // TODO do I need an async version of this? just return the future and don't have a blocking version?
  // caller would have to catch exceptions when calling get().
  // is it worth having an extra method just for that?
  // or would it be better to wrap ListenableFuture to catch the exception and have get() not declare any?
  // but that would require wrapping the executor service too. too much hassle.
  public ScenarioResults runScenarios(ViewConfig viewConfig,
                                      CalculationArguments calculationArguments,
                                      MarketDataEnvironment baseMarketData,
                                      ScenarioDefinition scenarioDefinition,
                                      List<?> portfolio) {

    // the outer set is the cycles, the list holds the perturbations to apply in that cycle
    List<List<SinglePerturbationMapping>> scenarios = scenarioDefinition.cyclePerturbations();
    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    List<ListenableFuture<Pair<String, Results>>> futures = new ArrayList<>();
    int scenarioNum = 1;

    for (List<SinglePerturbationMapping> scenario : scenarios) {
      // TODO need a better strategy for the scenario name. should the ScenarioDefinition do it?
      String scenarioName = Integer.toString(scenarioNum);
      scenarioNum += 1;
      ListenableFuture<Results> resultsFuture = runAsync(view, calculationArguments, baseMarketData, scenario, portfolio);
      futures.add(futureWithScenarioName(resultsFuture, scenarioName));
    }
    try {
      return scenarioResultsFuture(futures).get();
    } catch (InterruptedException | ExecutionException e) {
      // this will only happen if there's a bug in the engine, all exceptions should be caught and converted to results
      throw new OpenGammaRuntimeException("Failed to run scenarios", e);
    }
  }

  /**
   * Wraps a future so its return value includes the name of the scenario that produced its results.
   *
   * @param future a futures producing results for a calculation cycle
   * @param scenarioName the name of the scenario used in the calculation cycle
   * @return a future producing the scenario name and the scenario's calculation results
   */
  private ListenableFuture<Pair<String, Results>> futureWithScenarioName(ListenableFuture<Results> future,
                                                                         final String scenarioName) {
    // creates a new future that wraps the original future so it returns the scenario name along with its results
    return Futures.transform(
        future, new Function<Results, Pair<String, Results>>() {
          @Override
          public Pair<String, Results> apply(Results results) {
            return Pairs.of(scenarioName, results);
          }
        });
  }

  /**
   * Combines a list of futures producing a scenario name and results into one future producing {@link ScenarioResults}.
   *
   * @param futures futures producing a scenario name and results from running the scenario
   * @return a future producing results for all the scenarios
   */
  private ListenableFuture<ScenarioResults> scenarioResultsFuture(List<ListenableFuture<Pair<String, Results>>> futures) {
    ListenableFuture<List<Pair<String, Results>>> combinedFuture = Futures.allAsList(futures);
    return Futures.transform(combinedFuture, new Function<List<Pair<String, Results>>, ScenarioResults>() {

      @Override
      public ScenarioResults apply(List<Pair<String, Results>> scenarioResults) {
        ImmutableMap.Builder<String, Results> resultsBuilder = ImmutableMap.builder();

        for (Pair<String, Results> nameAndResults : scenarioResults) {
          resultsBuilder.put(nameAndResults.getKey(), nameAndResults.getValue());
        }
        return new ScenarioResults(resultsBuilder.build());
      }
    });
  }

  private ListenableFuture<Results> runAsync(View view,
                                             CalculationArguments calculationArguments,
                                             MarketDataEnvironment suppliedData,
                                             List<SinglePerturbationMapping> perturbations,
                                             List<?> inputs) {

    GatheringMarketDataBundle gatheringBundle = GatheringMarketDataBundle.create(suppliedData.toBundle());
    ZonedDateTime valuationTime = valuationTime(calculationArguments, suppliedData);
    view.run(calculationArguments, new GatheringMarketDataEnvironment(gatheringBundle, valuationTime), inputs);
    Set<MarketDataRequirement> requirements = gatheringBundle.getRequirements();
    MarketDataSpecification marketDataSpec = calculationArguments.getMarketDataSpecification();
    MarketDataEnvironment marketData =
        _environmentFactory.build(suppliedData, requirements, perturbations, marketDataSpec, valuationTime);
    return view.runAsync(calculationArguments, marketData, inputs);
  }

  private static ZonedDateTime valuationTime(CalculationArguments calcArgs, MarketDataEnvironment marketData) {
    if (calcArgs.getValuationTime() != null) {
      return calcArgs.getValuationTime();
    } else {
      return marketData.getValuationTime();
    }
  }
}
