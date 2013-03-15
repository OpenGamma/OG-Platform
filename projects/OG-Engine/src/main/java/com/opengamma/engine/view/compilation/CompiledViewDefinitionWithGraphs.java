/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.id.UniqueId;

/**
 * A {@link CompiledViewDefinition} with dependency graphs.
 */
public interface CompiledViewDefinitionWithGraphs extends CompiledViewDefinition {

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
   * @throw DataNotFoundException if the calculation configuration does not exist
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
