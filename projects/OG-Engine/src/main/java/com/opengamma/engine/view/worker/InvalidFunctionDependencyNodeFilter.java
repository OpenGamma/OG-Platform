/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.RootDiscardingSubgrapher;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;

/**
 * Filters a dependency graph to exclude any nodes that use functions that aren't valid for the valuation time.
 */
/* package */final class InvalidFunctionDependencyNodeFilter extends RootDiscardingSubgrapher {

  private final CompiledFunctionResolver _functions;
  private final Instant _valuationTime;
  private final Map<String, Boolean> _expired = new HashMap<String, Boolean>();

  public InvalidFunctionDependencyNodeFilter(final CompiledFunctionResolver functions, final Instant valuationTime) {
    _functions = functions;
    _valuationTime = valuationTime;
  }

  // RootDiscardingSubgrapher

  @Override
  protected boolean acceptNode(final DependencyNode node) {
    final String functionId = node.getFunction().getFunctionId();
    final Boolean expired = _expired.get(functionId);
    if (expired == null) {
      final CompiledFunctionDefinition cfd = _functions.getFunction(functionId);
      if (cfd == null) {
        // Function no longer in compiled repository
        _expired.put(functionId, Boolean.FALSE);
        return false;
      }
      Instant t = cfd.getEarliestInvocationTime();
      if (t != null) {
        if (_valuationTime.isBefore(t)) {
          // Function has expired
          _expired.put(functionId, Boolean.FALSE);
          return false;
        }
      }
      t = cfd.getLatestInvocationTime();
      if (t != null) {
        if (_valuationTime.isAfter(t)) {
          // Function has expired
          _expired.put(functionId, Boolean.FALSE);
          return false;
        }
      }
      // Function is still valid
      _expired.put(functionId, Boolean.TRUE);
      return true;
    }
    return expired.booleanValue();
  }

}
