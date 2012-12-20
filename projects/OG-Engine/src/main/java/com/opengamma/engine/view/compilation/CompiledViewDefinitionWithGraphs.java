/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import com.opengamma.engine.depgraph.DependencyGraphExplorer;

/**
 * A {@link CompiledViewDefinition} with dependency graphs.
 */
public interface CompiledViewDefinitionWithGraphs extends CompiledViewDefinition {

  /**
   * Gets a dependency graph explorer for a calculation configuration.
   * 
   * @param calcConfig  the calculation configuration, not null
   * @return the dependency graph explorer, not null
   */
  DependencyGraphExplorer getDependencyGraphExplorer(String calcConfig);
  
}
