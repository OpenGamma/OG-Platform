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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.sesame.config.EngineUtils;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MapScenarioMarketDataEnvironment;
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

  /** Runs tasks to gather market data requirements and build the data. */
  private final ListeningExecutorService _executor;

  /**
   * @param viewFactory for creating views for performing calculations using configuration passed into the run methods
   * @param environmentFactory builds market data in response to requirements gathered from the functions
   * @param executor run tasks to gather market data requirements and build the data
   */
  public DefaultEngine(
      ViewFactory viewFactory,
      MarketDataEnvironmentFactory environmentFactory,
      ExecutorService executor) {

    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "bundleBuilder");
    _viewFactory = ArgumentChecker.notNull(viewFactory, "viewFactory");
    _executor = MoreExecutors.listeningDecorator(ArgumentChecker.notNull(executor, "executor"));
  }

  @Override
  public Results runView(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment marketData,
      List<?> portfolio) {

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    return view.run(calculationArguments, marketData, portfolio);
  }

  @Override
  public ScenarioResults runScenarios(
      ViewConfig viewConfig,
      ScenarioMarketDataEnvironment scenarioMarketData,
      CalculationArguments calculationArguments,
      List<?> portfolio) {

    // the outer set is the cycles, the list holds the perturbations to apply in that cycle
    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    List<ListenableFuture<Pair<String, Results>>> resultFutures = new ArrayList<>();

    for (Map.Entry<String, MarketDataEnvironment> entry : scenarioMarketData.getData().entrySet()) {
      String scenarioName = entry.getKey();
      MarketDataEnvironment marketData = entry.getValue();

      // start running the view and return a future of the results
      ListenableFuture<Results> resultsFuture = view.runAsync(calculationArguments, marketData, portfolio);

      // create a future that wraps the results into a pair that includes the scenario name
      ListenableFuture<Pair<String, Results>> namedResultsFuture = futureWithScenarioName(resultsFuture, scenarioName);

      // add the future to the list of futures for all scenarios
      resultFutures.add(namedResultsFuture);
    }
    try {
      // create a future that combines the futures for all scenarios and return its result, blocking until it completes
      return scenarioResultsFuture(resultFutures).get();
    } catch (InterruptedException | ExecutionException e) {
      // this will only happen if there's a bug in the engine, all exceptions should be caught and converted to results
      throw new OpenGammaRuntimeException("Failed to run scenarios", e);
    }
  }

  @Override
  public MarketDataEnvironment buildMarketData(
      ViewConfig viewConfig,
      MarketDataEnvironment suppliedData,
      CalculationArguments calculationArguments,
      List<?> portfolio) {

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    Set<MarketDataRequirement> requirements = view.gatherRequirements(suppliedData, calculationArguments, portfolio);
    return _environmentFactory.build(
        suppliedData,
        requirements,
        ImmutableList.<SinglePerturbationMapping>of(),
        calculationArguments.getMarketDataSpecification(),
        calculationArguments.getValuationTime());
  }

  @Override
  public ScenarioMarketDataEnvironment buildScenarioMarketData(
      ViewConfig viewConfig,
      MarketDataEnvironment baseData,
      ScenarioDefinition scenarioDefinition,
      CalculationArguments calculationArguments,
      List<?> portfolio) {

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    // TODO when multiple valuation times are supported, gather one set of requirements for each valuation time
    Set<MarketDataRequirement> requirements = view.gatherRequirements(baseData, calculationArguments, portfolio);
    List<List<SinglePerturbationMapping>> scenarios = scenarioDefinition.cyclePerturbations();
    MarketDataSpecification marketDataSpecification = calculationArguments.getMarketDataSpecification();
    ZonedDateTime valuationTime = calculationArguments.getValuationTime();
    List<ListenableFuture<MarketDataEnvironment>> marketDataFutures = Lists.newArrayListWithExpectedSize(scenarios.size());

    for (List<SinglePerturbationMapping> scenario : scenarios) {
      // create a future for building the market data for this scenario
      marketDataFutures.add(marketDataFuture(baseData, requirements, scenario, marketDataSpecification, valuationTime));
    }
    // combine the market data futures for all scenarios into a single future
    ListenableFuture<List<MarketDataEnvironment>> combinedFuture = Futures.allAsList(marketDataFutures);
    try {
      // transform the future into a different future that yields a ScenarioMarketDataEnvironment
      return Futures.transform(
          combinedFuture,
          // TODO Java 8 - use a method reference this::buildScenarioData
          new Function<List<MarketDataEnvironment>, ScenarioMarketDataEnvironment>() {
            @Override
            public ScenarioMarketDataEnvironment apply(List<MarketDataEnvironment> marketDataList) {
              return buildScenarioData(marketDataList);
            }
          }).get();
    } catch (InterruptedException | ExecutionException e) {
      // this shouldn't happen unless there's a bug in the engine
      throw new OpenGammaRuntimeException("Failed to build market data", e);
    }
  }

  /**
   * Builds the market data for a set of scenarios given the market data for each of the individual scenarios.
   *
   * @param marketDataList list of market data for individual scenarios
   * @return the market data for a set of scenarios
   */
  private ScenarioMarketDataEnvironment buildScenarioData(List<MarketDataEnvironment> marketDataList) {
    ImmutableMap.Builder<String, MarketDataEnvironment> builder = ImmutableMap.builder();
    int scenarioIndex = 1;

    for (MarketDataEnvironment marketData : marketDataList) {
      // TODO a better way to name scenarios
      String scenarioName = Integer.toString(scenarioIndex);
      scenarioIndex++;
      builder.put(scenarioName, marketData);
    }
    return new MapScenarioMarketDataEnvironment(builder.build());
  }

  /**
   * Wraps a future so its return value includes the name of the scenario that produced its results.
   *
   * @param future a futures producing results for a calculation cycle
   * @param scenarioName the name of the scenario used in the calculation cycle
   * @return a future producing the scenario name and the scenario's calculation results
   */
  private ListenableFuture<Pair<String, Results>> futureWithScenarioName(
      ListenableFuture<Results> future,
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
    return Futures.transform(
        combinedFuture, new Function<List<Pair<String, Results>>, ScenarioResults>() {

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

  /**
   * Returns a future for asynchronously building the market data required to perform some calculations.
   * <p>
   * The future's task will be executed using the engine's thread pool.
   *
   * @param suppliedData market data supplied by the user
   * @param requirements requirements for data needed to perform the calculations but not supplied by the user
   * @param perturbations perturbations to apply to the market data in the current scenario
   * @param marketDataSpec specifies which market data providers should be used to look up the underlying market data
   * @param valuationTime valuation time for the calculations
   * @return a future for building the market data required to perform some calculations.
   */
  private ListenableFuture<MarketDataEnvironment> marketDataFuture(
      final MarketDataEnvironment suppliedData,
      final Set<MarketDataRequirement> requirements,
      final List<SinglePerturbationMapping> perturbations,
      final MarketDataSpecification marketDataSpec,
      final ZonedDateTime valuationTime) {

    // create a future that asynchronously builds the market data
    return _executor.submit(
        new Callable<MarketDataEnvironment>() {
          @Override
          public MarketDataEnvironment call() throws Exception {
            return _environmentFactory.build(suppliedData, requirements, perturbations, marketDataSpec, valuationTime);
          }
        });
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Everything below here is temporary and intended to ease migration from 2.8 to 2.9.
  // It will be removed in 2.10 or 3.0 at the latest

  @Override
  public ScenarioMarketDataEnvironment buildScenarioMarketData(
      ViewConfig viewConfig,
      ScenarioMarketDataEnvironment suppliedData,
      CalculationArguments calculationArguments,
      List<?> portfolio) {

    Map<String, MarketDataEnvironment> scenarioData = suppliedData.getData();

    if (scenarioData.isEmpty()) {
      return suppliedData;
    }
    Map.Entry<String, MarketDataEnvironment> firstScenario = scenarioData.entrySet().iterator().next();
    MarketDataEnvironment firstScenarioData = firstScenario.getValue();

    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    Set<MarketDataRequirement> requirements = view.gatherRequirements(firstScenarioData, calculationArguments, portfolio);
    MarketDataSpecification marketDataSpecification = calculationArguments.getMarketDataSpecification();
    ZonedDateTime valuationTime = calculationArguments.getValuationTime();
    List<ListenableFuture<Pair<String, MarketDataEnvironment>>> marketDataFutures =
        Lists.newArrayListWithExpectedSize(scenarioData.size());

    for (Map.Entry<String, MarketDataEnvironment> entry : scenarioData.entrySet()) {
      MarketDataEnvironment baseData = entry.getValue();
      String scenarioName = entry.getKey();
      // create a future for building the market data for this scenario
      marketDataFutures.add(marketDataFuture(scenarioName, baseData, requirements, marketDataSpecification, valuationTime));
    }
    // combine the market data futures for all scenarios into a single future
    ListenableFuture<List<Pair<String, MarketDataEnvironment>>> combinedFuture = Futures.allAsList(marketDataFutures);
    try {
      // transform the future into a different future that yields a ScenarioMarketDataEnvironment
      return Futures.transform(
          combinedFuture,
          new Function<List<Pair<String, MarketDataEnvironment>>, ScenarioMarketDataEnvironment>() {
            @Override
            public ScenarioMarketDataEnvironment apply(List<Pair<String, MarketDataEnvironment>> marketDataList) {
              return buildNamedScenarioData(marketDataList);
            }
          }).get();
    } catch (InterruptedException | ExecutionException e) {
      // this shouldn't happen unless there's a bug in the engine
      throw new OpenGammaRuntimeException("Failed to build market data", e);
    }
  }

  /**
   * Returns a future for asynchronously building the market data required to perform some calculations.
   * <p>
   * The future's task will be executed using the engine's thread pool.
   *
   * @param suppliedData market data supplied by the user
   * @param requirements requirements for data needed to perform the calculations but not supplied by the user
   * @param marketDataSpec specifies which market data providers should be used to look up the underlying market data
   * @param valuationTime valuation time for the calculations
   * @return a futures for building the market data required to perform some calculations.
   */
  private ListenableFuture<Pair<String, MarketDataEnvironment>> marketDataFuture(
      final String scenarioName,
      final MarketDataEnvironment suppliedData,
      final Set<MarketDataRequirement> requirements,
      final MarketDataSpecification marketDataSpec,
      final ZonedDateTime valuationTime) {

    // create a future that asynchronously builds the market data
    return _executor.submit(
        new Callable<Pair<String, MarketDataEnvironment>>() {
          @Override
          public Pair<String, MarketDataEnvironment> call() throws Exception {
            List<SinglePerturbationMapping> perturbations = ImmutableList.of();
            MarketDataEnvironment marketData =
                _environmentFactory.build(
                    suppliedData,
                    requirements,
                    perturbations,
                    marketDataSpec,
                    valuationTime);
            return Pairs.of(scenarioName, marketData);
          }
        });
  }

  /**
   * Builds the market data for a set of scenarios given the market data for each of the individual scenarios.
   *
   * @param marketDataList list of scenario names and the market data for the corresponding scenario
   * @return the market data for the scenarios
   */
  private ScenarioMarketDataEnvironment buildNamedScenarioData(List<Pair<String, MarketDataEnvironment>> marketDataList) {
    ImmutableMap.Builder<String, MarketDataEnvironment> builder = ImmutableMap.builder();

    for (Pair<String, MarketDataEnvironment> pair : marketDataList) {
      builder.put(pair.getKey(), pair.getValue());
    }
    return new MapScenarioMarketDataEnvironment(builder.build());
  }
}
