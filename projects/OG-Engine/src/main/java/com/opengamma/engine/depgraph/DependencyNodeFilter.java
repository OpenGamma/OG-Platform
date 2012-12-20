/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Strategy for filtering a dependency graph to produce a sub-graph.
 * <p>
 * This interface is used to decide whether a specific node should be
 * included in the sub-graph.
 */
public interface DependencyNodeFilter {

  /**
   * Determines whether to include the node in the sub-graph.
   * 
   * @param node  the node to examine, not null
   * @return true to include, false to exclude
   */
  boolean accept(DependencyNode node);

}
