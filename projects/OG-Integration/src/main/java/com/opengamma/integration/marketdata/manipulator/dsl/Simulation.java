/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.CompositeMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A collection of {@link Scenario}s, each of which modifies the market data in a single calculation cycle.
 * TODO create base scenario with no changes by default? constructor parameter with name / boolean?
 */
public class Simulation {

  private static final Logger s_logger = LoggerFactory.getLogger(Simulation.class);

  // TODO name field
  //private final String _name;
  /** The scenarios in this simulation, keyed by name. */
  private final Map<String, Scenario> _scenarios = Maps.newHashMap();
  /** The default calculation configuration name for scenarios. */
  private final Set<String> _calcConfigNames;
  /** The default valuation time for scenarios. */
  private final Instant _valuationTime;
  /** The default resolver version correction for scenarios. */
  private final VersionCorrection _resolverVersionCorrection;

  /**
   * Creates a new simulation with a calcuation configuration name of "Default", valuation time of {@code Instant.now()}
   * and resolver version correction of {@link VersionCorrection#LATEST}.
   */
  public Simulation() {
    this(Instant.now(), VersionCorrection.LATEST);
  }

  /**
   * Creates a new simulation, specifying the default values to use for its scenarios
   * @param calcConfigNames The default calculation configuration name for scenarios
   * @param valuationTime The default valuation time for scenarios
   * @param resolverVersionCorrection The default resolver version correction for scenarios
   */
  public Simulation(Instant valuationTime, VersionCorrection resolverVersionCorrection, String... calcConfigNames) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    if (calcConfigNames.length > 0) {
      _calcConfigNames = ImmutableSet.copyOf(calcConfigNames);
    } else {
      _calcConfigNames = null;
    }
    _valuationTime = valuationTime;
    _resolverVersionCorrection = resolverVersionCorrection;
  }

  /* package */ Set<DistinctMarketDataSelector> allSelectors() {
    // TODO check for empty scenarios
    Set<DistinctMarketDataSelector> selectors = Sets.newHashSet();
    for (Scenario scenario : _scenarios.values()) {
      selectors.addAll(scenario.createDefinition().getDefinitionMap().keySet());
    }
    return Collections.unmodifiableSet(selectors);
  }

  /**
   * Builds cycle execution options for each scenario in this simulation.
   * @param baseOptions Base set of options
   * @param allSelectors This simulation's selectors
   * @return Execution options for each scenario in this simulation
   */
  /* package */ List<ViewCycleExecutionOptions> cycleExecutionOptions(ViewCycleExecutionOptions baseOptions,
                                                                      Set<DistinctMarketDataSelector> allSelectors) {
    List<ViewCycleExecutionOptions> options = Lists.newArrayListWithCapacity(_scenarios.size());
    for (Scenario scenario : _scenarios.values()) {
      ScenarioDefinition definition = scenario.createDefinition();
      Map<DistinctMarketDataSelector, FunctionParameters> scenarioParams = definition.getDefinitionMap();
      Map<DistinctMarketDataSelector, FunctionParameters> params = Maps.newHashMap();
      params.putAll(scenarioParams);
      // if a selector isn't used by a particular scenario then it needs to have a no-op manipulatior. if it didn't
      // then the manipulator from the previous scenario would be used
      Set<DistinctMarketDataSelector> unusedSelectors = Sets.difference(allSelectors, params.keySet());
      for (DistinctMarketDataSelector unusedSelector : unusedSelectors) {
        params.put(unusedSelector, EmptyFunctionParameters.INSTANCE);
      }
      ViewCycleExecutionOptions scenarioOptions = baseOptions.copy()
          .setFunctionParameters(params)
          .setValuationTime(scenario.getValuationTime())
          .setResolverVersionCorrection(scenario.getResolverVersionCorrection())
          .create();
      options.add(scenarioOptions);
    }
    return options;
  }

  /**
   * Returns the scenario with the given name. If no scenario exists with the specified name it is created and
   * initialized with default the simulation's default values for calculation configuration, valuation time and
   * resolver version correction.
   * @param name The scenario name
   * @return The scenario.
   * TODO check the name isn't the base scenario name and throw IAE
   */
  public Scenario scenario(String name) {
    if (_scenarios.containsKey(name)) {
      return _scenarios.get(name);
    } else {
      Scenario scenario = new Scenario(name, _calcConfigNames, _valuationTime, _resolverVersionCorrection);
      _scenarios.put(name, scenario);
      return scenario;
    }
  }

  /**
   * Executes this simulation on a running server.
   * @param viewDefId The ID of the view definition to use
   * @param marketDataSpecs The market data to use when running the view
   * @param batchMode Whether to run the simulation using batch mode
   * @param listener Listener that is notified as the simulation runs
   * @param viewProcessor View process that will be used to execute the simulation
   */
  public void run(UniqueId viewDefId,
                  List<MarketDataSpecification> marketDataSpecs,
                  boolean batchMode,
                  ViewResultListener listener,
                  ViewProcessor viewProcessor) {
    ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getTestUser());
    try {
      Set<DistinctMarketDataSelector> allSelectors = allSelectors();
      ViewCycleExecutionOptions baseOptions =
          ViewCycleExecutionOptions
              .builder()
              .setMarketDataSpecifications(marketDataSpecs)
              .setMarketDataSelector(CompositeMarketDataSelector.of(allSelectors))
              .create();
      List<ViewCycleExecutionOptions> cycleOptions = cycleExecutionOptions(baseOptions, allSelectors);
      ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(cycleOptions);
      EnumSet<ViewExecutionFlags> executionFlags = ExecutionFlags.none().awaitMarketData().runAsFastAsPossible().get();
      ViewExecutionOptions executionOptions;
      if (listener != null) {
        viewClient.setResultListener(listener);
      }
      if (batchMode) {
        executionOptions = ExecutionOptions.batch(sequence, baseOptions);
      } else if (listener != null) {
        executionOptions = ExecutionOptions.of(sequence, executionFlags);
      } else {
        s_logger.warn("Not running in batch mode and no listener specifed, the results would be ignored. Exiting.");
        return;
      }
      viewClient.attachToViewProcess(viewDefId, executionOptions, true);
      try {
        viewClient.waitForCompletion();
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted waiting for ViewClient to complete", e);
      }
    } finally {
      viewClient.shutdown();
    }
  }

  @Override
  public String toString() {
    return "Simulation [" +
        "_scenarios=" + _scenarios +
        ", _calcConfigNames=" + _calcConfigNames +
        ", _valuationTime=" + _valuationTime +
        ", _resolverVersionCorrection=" + _resolverVersionCorrection +
        "]";
  }
}
