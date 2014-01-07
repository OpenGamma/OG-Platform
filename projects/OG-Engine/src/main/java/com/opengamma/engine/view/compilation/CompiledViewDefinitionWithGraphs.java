/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A {@link CompiledViewDefinition} with dependency graphs.
 */
public interface CompiledViewDefinitionWithGraphs extends CompiledViewDefinition {

  /**
   * Returns a copy of this object with an updated version/correction parameter.
   * 
   * @param resolverVersionCorrection the resolver version/correction date for the copy
   * @return the copy
   */
  @Override
  CompiledViewDefinitionWithGraphs withResolverVersionCorrection(VersionCorrection resolverVersionCorrection);

  /**
   * Returns a copy of this object with updated market data manipulation selections.
   * 
   * @param newGraphsByConfig the updated dependency graphs, not null
   * @param selectionsByConfig the market data selections that have been identified for each graph, not null
   * @param paramsByConfig the function parameters that have been defined for each graph, not null
   * @return the copy
   */
  CompiledViewDefinitionWithGraphs withMarketDataManipulationSelections(
      Map<String, DependencyGraph> newGraphsByConfig,
      Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig,
      Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfig);

  /**
   * Gets all of the dependency graph explorers.
   * 
   * @return the dependency graph explorers, not null
   */
  Collection<DependencyGraphExplorer> getDependencyGraphExplorers();

  /**
   * Gets a dependency graph explorer for a calculation configuration.
   * 
   * @param calcConfig the calculation configuration, not null
   * @return the dependency graph explorer, not null
   * @throws DataNotFoundException if the calculation configuration does not exist
   */
  DependencyGraphExplorer getDependencyGraphExplorer(String calcConfig);

  /**
   * Gets the object and external identifiers that were resolved as part of the view compilation. The graphs contained in this instance are only valid when the mapping returned here holds. For
   * example, a different version/correction used for target resolution might make one or more references resolve to a different target specification. Anything using the original specification will
   * not longer be valid.
   * 
   * @return the map of target references containing object identifiers (unversioned unique identifiers) or external identifiers to the resolved unique identifiers
   */
  Map<ComputationTargetReference, UniqueId> getResolvedIdentifiers();
}
