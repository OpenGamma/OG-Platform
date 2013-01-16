/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.target.ComputationTargetType;

/**
 * Filters a dependency graph to exclude any nodes with a PORTFOLIO or PORTFOLIO_NODE target.
 */
/* package */final class InvalidPortfolioDependencyNodeFilter implements DependencyNodeFilter {

  // DependencyNodeFilter

  @Override
  public boolean accept(final DependencyNode node) {
    final ComputationTargetType nodeType = node.getComputationTarget().getType();
    return !nodeType.isTargetType(ComputationTargetType.PORTFOLIO_NODE) && !nodeType.isTargetType(ComputationTargetType.PORTFOLIO);
  }

}
