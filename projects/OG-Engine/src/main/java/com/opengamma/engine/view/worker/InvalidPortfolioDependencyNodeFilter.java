/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Set;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.RootDiscardingSubgrapher;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 * Filters a dependency graph to exclude any nodes with a PORTFOLIO or PORTFOLIO_NODE target.
 */
/* package */final class InvalidPortfolioDependencyNodeFilter extends RootDiscardingSubgrapher {

  private final Set<UniqueId> _badNodes;

  public InvalidPortfolioDependencyNodeFilter(final Set<UniqueId> badNodes) {
    _badNodes = badNodes;
  }

  // RootDiscardingSubgrapher

  @Override
  public boolean acceptNode(final DependencyNode node) {
    final ComputationTargetType nodeType = node.getTarget().getType();
    if (nodeType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
      return !_badNodes.contains(node.getTarget().getUniqueId());
    }
    return !nodeType.isTargetType(ComputationTargetType.PORTFOLIO);
  }

}
