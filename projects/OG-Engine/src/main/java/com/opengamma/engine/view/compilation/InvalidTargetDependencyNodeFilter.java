/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Set;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.RootDiscardingSubgrapher;
import com.opengamma.id.UniqueId;

/**
 * Filters a dependency graph to exclude any nodes that have targets in an invalid set.
 */
public final class InvalidTargetDependencyNodeFilter extends RootDiscardingSubgrapher {

  private final Set<UniqueId> _invalidTargets;

  public InvalidTargetDependencyNodeFilter(final Set<UniqueId> invalidTargets) {
    _invalidTargets = invalidTargets;
  }

  // RootDiscardingSugrapher

  @Override
  public boolean acceptNode(final DependencyNode node) {
    final UniqueId uid = node.getTarget().getUniqueId();
    return (uid == null) || !_invalidTargets.contains(uid);
  }

}
