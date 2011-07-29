/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Computation target filter that allows any node that has a specific target
 * specification, either itself or in a dependent node.
 */
public class ApplyToSubtree implements ComputationTargetFilter {

  /**
   * The target specification to match.
   */
  private final ComputationTargetSpecification _subtreeRoot;

  /**
   * Creates an instance accepting the specified target specification.
   * 
   * @param subtreeRoot  the desired target specification, not null
   */
  public ApplyToSubtree(ComputationTargetSpecification subtreeRoot) {
    ArgumentChecker.notNull(subtreeRoot, "Subtree root");
    _subtreeRoot = subtreeRoot;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean accept(DependencyNode node) {
    while (node != null) {
      if (node.getComputationTarget().toSpecification().equals(_subtreeRoot)) {
        return true;
      }
      node = node.getDependentNode();
    }
    return false;
  }

}
