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
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

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
 */
public class Simulation {

  private static final Logger s_logger = LoggerFactory.getLogger(Simulation.class);

  /** The simulation name. */
  private final String _name; // TODO this needs to be passed to the results somehow
  /** The scenarios in this simulation, keyed by name. */
  private final Map<String, Scenario> _scenarios = Maps.newLinkedHashMap();

  /** The default calculation configuration name for scenarios. */
  private Set<String> _calcConfigNames;
  /** The default valuation time for scenarios. */
  private Instant _valuationTime;
  /** The default resolver version correction for scenarios. */
  private VersionCorrection _resolverVersionCorrection;

  /** The name of the base scenario (i.e. containing no transformations) */
  private String _baseScenarioName;

  /**
   * Creates a new simulation with a calculation configuration name of "Default", valuation time of {@code Instant.now()}
   * and resolver version correction of {@link VersionCorrection#LATEST}.
   * @param name The simulation name
   */
  public Simulation(String name) {
    ArgumentChecker.notEmpty(name, "name");
    _name = name;
  }

  /**
   * Creates a new simulation, specifying the default values to use for its scenarios
   * @param name The simulation name
   * @param calcConfigNames The default calculation configuration name for scenarios
   * @param valuationTime The default valuation time for scenarios
   * @param resolverVersionCorrection The default resolver version correction for scenarios
   */
  public Simulation(String name, Instant valuationTime, VersionCorrection resolverVersionCorrection, String... calcConfigNames) {
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _name = name;
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
      // if a selector isn't used by a particular scenario then it needs to have a no-op manipulator. if it didn't
      // then the manipulator from the previous scenario would be used
      Set<DistinctMarketDataSelector> unusedSelectors = Sets.difference(allSelectors, params.keySet());
      for (DistinctMarketDataSelector unusedSelector : unusedSelectors) {
        params.put(unusedSelector, EmptyFunctionParameters.INSTANCE);
      }
      ViewCycleExecutionOptions scenarioOptions = baseOptions.copy()
          .setName(definition.getName())
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
    ArgumentChecker.notEmpty(name, "name");
    if (name.equals(_baseScenarioName)) {
      throw new IllegalArgumentException("Can't add scenario named " + name + ", a base scenario exists with " +
                                             "that name");
    }
    if (_scenarios.containsKey(name)) {
      return _scenarios.get(name);
    } else {
      Scenario scenario = new Scenario(this, name);
      _scenarios.put(name, scenario);
      return scenario;
    }
  }

  /**
   * Creates a base scenario with the given name. A base scenario has no transformations defined.
   * @param name The name of the base scenario
   * @return This simulation
   * @throws IllegalStateException If the base scenario name has already been set
   * @throws IllegalArgumentException If there is already a non-base scenario with the specified name
   */
  public Simulation baseScenarioName(String name) {
    ArgumentChecker.notEmpty(name, "name");
    if (_baseScenarioName != null) {
      throw new IllegalStateException("Base scenario already defined with name " + _baseScenarioName);
    }
    if (_scenarios.containsKey(name)) {
      throw new IllegalArgumentException("Cannot add a base scenario named " + name + ", a scenario already exists " +
                                             "with that name");
    }
    Scenario base = new Scenario(this, name);
    _scenarios.put(name, base);
    _baseScenarioName = name;
    return this;
  }

  /**
   * Sets the calculation configuration name to which the scenarios will apply.
   * @param calcConfigNames The calculation configuration name to which the scenarios will apply
   * @return This simulation
   * @throws IllegalStateException If the calculation configuration names have already been set
   */
  public Simulation calculationConfigurations(String... calcConfigNames) {
    ArgumentChecker.notEmpty(calcConfigNames, "calcConfigNames");
    if (_calcConfigNames != null) {
      throw new IllegalStateException("Calculation configuration names are already set");
    }
    _calcConfigNames = ImmutableSet.copyOf(calcConfigNames);
    return this;
  }

  /**
   * Sets the {@link VersionCorrection} used when resolving positions and portfolios
   * @param versionCorrection The version/correction used when resolving positions and portfolios
   * @return This simulation
   */
  public Simulation resolverVersionCorrection(VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    if (_resolverVersionCorrection != null) {
      throw new IllegalStateException("Resolver version correction has already been set");
    }
    _resolverVersionCorrection = versionCorrection;
    return this;
  }

  /**
   * Sets the valuation time used in the calculations
   * @param valuationTime The valuation time used in the calculations
   * @return This simulation
   * @throws IllegalStateException If the valuation time has already been set
   */
  public Simulation valuationTime(Instant valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    if (_valuationTime != null) {
      throw new IllegalStateException("Valuation time has already been set");
    }
    _valuationTime = valuationTime;
    return this;
  }

  /**
   * Sets the valuation time used in the calculations
   * @param valuationTime The valuation time used in the calculations
   * @return This simulation
   * @throws IllegalStateException If the valuation time has already been set
   */
  public Simulation valuationTime(ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    return valuationTime(valuationTime.toInstant());
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
        executionOptions = ExecutionOptions.of(sequence, baseOptions, executionFlags);
      } else {
        s_logger.warn("Not running in batch mode and no listener specified, the results would be ignored. Exiting.");
        return;
      }
      s_logger.info("Attaching to view process, view def ID {}, execution options {}", viewDefId, executionOptions);
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

  /* package */ Set<String> getCalcConfigNames() {
    return _calcConfigNames;
  }

  /* package */ Instant getValuationTime() {
    return _valuationTime;
  }

  /* package */ VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  /**
   * @return The scenarios keyed by name. The map's iterator returns the scenarios in the order they were created.
   */
  /* package */ Map<String, Scenario> getScenarios() {
    return Collections.unmodifiableMap(_scenarios);
  }

  /* package */ List<String> getScenarioNames() {
    return Lists.newArrayList(_scenarios.keySet());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _scenarios, _calcConfigNames, _valuationTime, _resolverVersionCorrection, _baseScenarioName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Simulation other = (Simulation) obj;
    return Objects.equals(this._name, other._name) &&
        Objects.equals(this._scenarios, other._scenarios) &&
        Objects.equals(this._calcConfigNames, other._calcConfigNames) &&
        Objects.equals(this._valuationTime, other._valuationTime) &&
        Objects.equals(this._resolverVersionCorrection, other._resolverVersionCorrection) &&
        Objects.equals(this._baseScenarioName, other._baseScenarioName);
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
