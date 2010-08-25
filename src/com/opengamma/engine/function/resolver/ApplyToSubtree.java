/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ApplyToSubtree implements ComputationTargetFilter {
  
  private final ComputationTargetSpecification _subtreeRoot;
  
  public ApplyToSubtree(ComputationTargetSpecification subtreeRoot) {
    ArgumentChecker.notNull(subtreeRoot, "Subtree root");
    _subtreeRoot = subtreeRoot;
  }

  @Override
  public boolean accept(DependencyNode node) {
    return false;
  }
  
}
