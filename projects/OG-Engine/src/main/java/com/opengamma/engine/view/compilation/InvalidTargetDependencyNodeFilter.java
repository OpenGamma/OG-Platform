/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Set;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.id.UniqueId;

/**
 * Filters a dependency graph to exclude any nodes that have targets in an invalid set.
 */
public final class InvalidTargetDependencyNodeFilter implements DependencyNodeFilter {

  private final Set<UniqueId> _invalidTargets;

  public InvalidTargetDependencyNodeFilter(final Set<UniqueId> invalidTargets) {
    _invalidTargets = invalidTargets;
  }

  // DependencyNodeFilter

  @Override
  public boolean accept(final DependencyNode node) {
    final UniqueId uid = node.getComputationTarget().getUniqueId();
    return (uid == null) || !_invalidTargets.contains(uid);
  }

}
