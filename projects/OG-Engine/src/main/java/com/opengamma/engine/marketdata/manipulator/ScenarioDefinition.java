package com.opengamma.engine.marketdata.manipulator;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.Config;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable class defining a scenario which holds a map of the market
 * data manipulation targets (e.g. USD 3M Yield Curve) and the manipulations
 * to be performed (e.g. shift by +10bps).
 *
 * ScenarioDefinitions can be stored in the config master and used in the
 * setup of ViewDefinitions.
 */
@Config
public class ScenarioDefinition {

  private final String _name;
  private final Map<DistinctMarketDataSelector, FunctionParameters> _definitionMap;

  public ScenarioDefinition(String name, Map<DistinctMarketDataSelector, FunctionParameters> definitionMap) {
    ArgumentChecker.notEmpty(name, "name");
    _name = name;
    _definitionMap = ImmutableMap.copyOf(definitionMap);
  }

  /**
   * Return an immutable map of the market data selectors to function parameters.
   *
   * @return market data to function parameters mapping
   */
  public Map<DistinctMarketDataSelector, FunctionParameters> getDefinitionMap() {
    return _definitionMap;
  }

  /**
   * @return The scenario name, not null
   */
  public String getName() {
    return _name;
  }
}
