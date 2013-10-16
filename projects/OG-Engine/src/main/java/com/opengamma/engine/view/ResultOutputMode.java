/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;

/**
 * Enumerates the different output modes that can apply to categories of results.
 */
@PublicAPI
public enum ResultOutputMode {

  /**
   * Indicates that no results should be included in the output. Use this setting to hide particularly large categories of results if they are not needed. Note that the use of this setting may result
   * in dependency graph optimisations which cause intermediate calculations to be optimised away; do not use this setting on targets which are entirely driving upstream intermediate calculations if
   * those results are required.
   */
  NONE {

    @Override
    public boolean shouldOutputResult(final ValueSpecification outputSpecification, final DependencyGraph dependencyGraph) {
      return false;
    }

  },

  /**
   * Indicates that only terminal outputs should be included, i.e. those outputs that are explicitly required by the {@link ViewCalculationConfiguration}. This should be the default setting for
   * satisfying all user requirements when 'explain'-type functionality is not necessary.
   */
  TERMINAL_OUTPUTS {

    @Override
    public boolean shouldOutputResult(final ValueSpecification outputSpecification, final DependencyGraph dependencyGraph) {
      return dependencyGraph.getTerminalOutputs().containsKey(outputSpecification);
    }

  },

  /**
   * Indicates that all outputs should be included. This setting would be necessary for introspecting the dependency graph in order to explain a terminal output. The use of this setting rather than {
   * {@link #TERMINAL_OUTPUTS} will never result in more calculations taking place, but will cause any intermediate results to be included in the output rather than being discarded.
   */
  ALL {

    @Override
    public boolean shouldOutputResult(final ValueSpecification outputSpecification, final DependencyGraph dependencyGraph) {
      return true;
    }

  };

  /**
   * Indicates whether a particular output value should be included in the results.
   * 
   * @param outputSpecification the specification of the output value, not null
   * @param dependencyGraph the dependency graph to which the output value belongs, not null
   * @return <code>true</code> if the output should be included in the results, <code>false</code> otherwise.
   */
  public abstract boolean shouldOutputResult(final ValueSpecification outputSpecification, final DependencyGraph dependencyGraph);

}
