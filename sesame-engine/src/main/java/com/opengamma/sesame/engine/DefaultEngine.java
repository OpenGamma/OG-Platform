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
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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

  /** Runs tasks to gather market data requirements and build the data. */
  private final ListeningExecutorService _executor;

  /**
   * @param viewFactory for creating views for performing calculations using configuration passed into the run methods
   * @param environmentFactory builds market data in response to requirements gathered from the functions
   * @param executor run tasks to gather market data requirements and build the data
   */
  public DefaultEngine(ViewFactory viewFactory,
                       MarketDataEnvironmentFactory environmentFactory,
                       ExecutorService executor) {
    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "bundleBuilder");
    _viewFactory = ArgumentChecker.notNull(viewFactory, "viewFactory");
    _executor = MoreExecutors.listeningDecorator(ArgumentChecker.notNull(executor, "executor"));
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

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param calculationArguments arguments used when performing the calculations for the scenarios
   * @param suppliedData contains the market data for the scenarios
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   * @deprecated this is here for backwards compatibility. It will be removed after v2.9
   */
  @Deprecated
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
                                      MarketDataEnvironment suppliedData,
                                      ScenarioDefinition scenarioDefinition,
                                      List<?> portfolio) {

    // the outer set is the cycles, the list holds the perturbations to apply in that cycle
    List<List<SinglePerturbationMapping>> scenarios = scenarioDefinition.cyclePerturbations();
    View view = _viewFactory.createView(viewConfig, EngineUtils.getInputTypes(portfolio));
    List<ListenableFuture<Pair<String, Results>>> resultFutures = new ArrayList<>();
    Set<MarketDataRequirement> requirements = gatherRequirements(suppliedData, calculationArguments, view, portfolio);
    ZonedDateTime valuationTime = valuationTime(calculationArguments, suppliedData);
    int scenarioNum = 1;

    for (List<SinglePerturbationMapping> scenario : scenarios) {
      MarketDataSpecification marketDataSpecification = calculationArguments.getMarketDataSpecification();
      // TODO need a better strategy for the scenario name. should the ScenarioDefinition do it?
      String scenarioName = Integer.toString(scenarioNum);
      scenarioNum += 1;

      // set up a pipeline of futures to asynchronously build the market data, run the view and wrap the result

      // create a future for building the market data for this scenario
      ListenableFuture<MarketDataEnvironment> marketDataFuture =
          marketDataFuture(suppliedData, requirements, scenario, marketDataSpecification, valuationTime);

      // create a function that takes the market data and returns a future for running the view
      AsyncFunction<MarketDataEnvironment, Results> runViewFunction =
          runViewFunction(portfolio, view, calculationArguments);

      // apply the function to the market data future to create a future that builds data and uses it to run the view
      ListenableFuture<Results> viewFuture = Futures.transform(marketDataFuture, runViewFunction);

      // create a future that wraps the results into a pair that includes the scenario name
      ListenableFuture<Pair<String, Results>> resultsFuture = futureWithScenarioName(viewFuture, scenarioName);

      // add the future to the list of futures for all scenarios
      resultFutures.add(resultsFuture);
    }
    try {
      // create a future that combines the futures for all scenarios and return its result, blocking until it completes
      return scenarioResultsFuture(resultFutures).get();
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

  /**
   * Runs a view without any market data and records the market data that is requested by the functions.
   * <p>
   * This runs the view once using the data from the first scenario as the set of supplied data. This
   * implicitly assumes that all scenarios contain the same set of market data, although possibly with different
   * values. It also assumes all scenarios use the same valuation time. Neither of these assumptions are provably
   * correct, although in practice they are true for all users of the engine.
   * <p>
   * Without this simplifying assumption it would be necessary to run the view twice for each scenario and
   * performance would be unacceptably poor. There are changes planned to the engine API for v2.9 that will
   * make this assumption always true.
   *
   * @param baseData base data supplied by the user
   * @param calculationArguments arguments specifying how the calculation should be performed
   * @param view the view which defines the calculations
   * @param portfolio the portfolio for which the calculations will be performed
   * @return the market data requirements for running the scenarios
   */
  private static Set<MarketDataRequirement> gatherRequirements(MarketDataEnvironment baseData,
                                                               CalculationArguments calculationArguments,
                                                               View view,
                                                               List<?> portfolio) {
    GatheringMarketDataBundle gatheringBundle = GatheringMarketDataBundle.create(baseData.toBundle());
    ZonedDateTime valuationTime = valuationTime(calculationArguments, baseData);
    view.run(calculationArguments, new GatheringMarketDataEnvironment(gatheringBundle, valuationTime), portfolio);
    return gatheringBundle.getRequirements();
  }

  /**
   * Creates a function to transform a future into another future. This is for use with {@code Futures.transform}.
   * The input future produces market data that was built by the engine. This function consumes the market data
   * and returns a future that uses the market data to run a view.
   * <p>
   * The market data built by the engine is combined with the market data supplied by the user before executing
   * the view.
   *
   * @param portfolio the portfolio to use when running the view
   * @param view the view to run
   * @param calculationArgs arguments specifying how to perform the calculations
   * @return a function for transforming a future
   */
  private AsyncFunction<MarketDataEnvironment, Results> runViewFunction(final List<?> portfolio,
                                                                        final View view,
                                                                        final CalculationArguments calculationArgs) {
    return new AsyncFunction<MarketDataEnvironment, Results>() {
      @Override
      public ListenableFuture<Results> apply(MarketDataEnvironment marketData) {
        return view.runAsync(calculationArgs, marketData, portfolio);
      }
    };
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
   * @return a futures for building the market data required to perform some calculations.
   */
  private ListenableFuture<MarketDataEnvironment> marketDataFuture(final MarketDataEnvironment suppliedData,
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

}
