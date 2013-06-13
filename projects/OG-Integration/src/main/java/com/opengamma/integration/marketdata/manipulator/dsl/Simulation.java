/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class Simulation {

  private final List<Scenario> _scenarios;
  //private final String _name;
  private final Set<DistinctMarketDataSelector> _allSelectors;
  private static final SimpleFunctionParameters NOOP_FUNCTION_PARAMETERS;

  static {
    NOOP_FUNCTION_PARAMETERS = new SimpleFunctionParameters();
    NoOpStructureManipulator<Object> noOpManipulator = new NoOpStructureManipulator<>();
    NOOP_FUNCTION_PARAMETERS.setValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME, noOpManipulator);
  }

  private Simulation(List<Scenario> scenarios) {
    ArgumentChecker.notEmpty(scenarios, "scenarios");
    _scenarios = ImmutableList.copyOf(scenarios);
    //_name = null;
    // TODO check for empty scenarios
    Set<DistinctMarketDataSelector> selectors = Sets.newHashSet();
    for (Scenario scenario : _scenarios) {
      selectors.addAll(scenario.getMarketDataManipulations().keySet());
    }
    _allSelectors = Collections.unmodifiableSet(selectors);
  }

  public Set<DistinctMarketDataSelector> allSelectors() {
    return _allSelectors;
  }

  public List<ViewCycleExecutionOptions> cycleExecutionOptions(ViewCycleExecutionOptions baseOptions) {
    List<ViewCycleExecutionOptions> options = Lists.newArrayListWithCapacity(_scenarios.size());
    for (Scenario scenario : _scenarios) {
      Map<DistinctMarketDataSelector, FunctionParameters> scenarioParams = scenario.getMarketDataManipulations();
      Map<DistinctMarketDataSelector, FunctionParameters> params = Maps.newHashMap();
      params.putAll(scenarioParams);
      // TODO confirm this is necessary, parameters might be cleared before every cycle
      // if a selector isn't used by a particular scenario then it needs to have a no-op manipulatior. if it didn't
      // then the manipulator from the previous scenario would be used
      Set<DistinctMarketDataSelector> unusedSelectors = Sets.difference(_allSelectors, params.keySet());
      for (DistinctMarketDataSelector unusedSelector : unusedSelectors) {
        params.put(unusedSelector, NOOP_FUNCTION_PARAMETERS);
      }
      // TODO different valuation time for each scenario
      ViewCycleExecutionOptions scenarioOptions = baseOptions.copy().setFunctionParameters(params).create();
      options.add(scenarioOptions);
    }
    return options;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final List<Scenario> _scenarios = Lists.newArrayList();

    public Scenario addScenario() {
      // scenario needs a ref to the simulation but it doesn't exist yet, only the builder does
      Scenario scenario = new Scenario();
      _scenarios.add(scenario);
      return scenario;
    }

    public Simulation build() {
      return new Simulation(_scenarios);
    }
  }
}
