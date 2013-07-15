/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import org.threeten.bp.Instant;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.CompiledFunctionDefinition;

/**
 * Filters a dependency graph to exclude any nodes that use functions that aren't valid for the valuation time.
 */
/* package */final class InvalidFunctionDependencyNodeFilter implements DependencyNodeFilter {

  private final Instant _valuationTime;

  public InvalidFunctionDependencyNodeFilter(final Instant valuationTime) {
    _valuationTime = valuationTime;
  }

  // DependencyNodeFilter

  @Override
  public boolean accept(final DependencyNode node) {
    final CompiledFunctionDefinition cfd = node.getFunction().getFunction();
    Instant t = cfd.getEarliestInvocationTime();
    if (t != null) {
      if (_valuationTime.isBefore(t)) {
        return false;
      }
    }
    t = cfd.getLatestInvocationTime();
    if (t != null) {
      if (_valuationTime.isAfter(t)) {
        return false;
      }
    }
    return true;
  }

}
