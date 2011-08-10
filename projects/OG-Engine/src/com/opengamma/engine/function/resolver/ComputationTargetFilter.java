/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;

/**
 * 
 */
public abstract class ComputationTargetFilter implements DependencyNodeFilter {

  @Override
  public boolean accept(DependencyNode node) {
    return accept(node.getComputationTarget());
  }

  public abstract boolean accept(ComputationTarget target);

}
