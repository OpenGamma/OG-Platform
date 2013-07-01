/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Provides access to a snapshot of the basic state involved in the computation of an individual view calculation configuration.
 */
public interface CompiledViewCalculationConfiguration {

  /**
   * Gets the name of the view calculation configuration.
   * 
   * @return the name of the view calculation configuration, not null
   */
  String getName();

  /**
   * Gets the map of terminal output {@link ValueSpecification} to all satisfying requirements {@link ValueRequirement} for the calculation configuration.
   * 
   * @return the map of terminal output {@link ValueSpecification} to all satisfying requirements {@link ValueRequirement} for the calculation configuration., not null
   */
  Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputSpecifications();

  /**
   * Gets the set of terminal output values for the calculation configuration.
   * 
   * @return the set of terminal output values, not null
   */
  Set<Pair<String, ValueProperties>> getTerminalOutputValues();

  /**
   * Gets the computation targets for the calculation configuration.
   * 
   * @return the set of computation targets, not null
   */
  Set<ComputationTargetSpecification> getComputationTargets();

  /**
   * Gets the market data requirements of the calculation configuration.
   * 
   * @return the value specifications for all required market data, not null
   */
  Set<ValueSpecification> getMarketDataRequirements();

  /**
   * Gets any aliases of requested market data requirements. Aliases occur when there is a mismatch between how targets are quoted or referenced by the function repository and how they are specified
   * by a market data provider.
   * <p>
   * For example a target might be specified by a function as a security unique identifier, a market data provider might need to use the unique identifier of the external system the data it to be
   * sourced from, and there is nothing in the security definition to provide that mapping. The mapping is then captured by the presence of value aliasing nodes in the dependency graph.
   * 
   * @return the value specification aliases of market data as underlying requirements mapped to the aliased values, not null. The keys of this map will correspond to the set returned by
   *         {@link #getMarketDataRequirements}.
   */
  Map<ValueSpecification, Collection<ValueSpecification>> getMarketDataAliases();

  /**
   * Gets the results of any market data selections that have been made to support manipulation of the structured market data.
   * <p>
   * The map contains any selector that matched a market data structure in the dependency graph which are mapped to the
   * value specifications of the new nodes that have been added to the graph to allow manipulation.
   *
   * @return the mapping of active market data selectors to the value specs that allow manipulation, not null
   */
  Map<DistinctMarketDataSelector, Set<ValueSpecification>> getMarketDataSelections();

  /**
   * Gets any function parameters that have been defined for market data selections. The keys of this map
   * should be a subset of the keys from the {@link #getMarketDataSelections()} map. There may be
   * fewer keys in this map as function parameters can also be introduced through ViewCycleExecutionOptions.
   *
   * @return the mapping of active market data selectors to the function parameters that perform manipulation, not null
   */
  Map<DistinctMarketDataSelector, FunctionParameters> getMarketDataSelectionFunctionParameters();

}
