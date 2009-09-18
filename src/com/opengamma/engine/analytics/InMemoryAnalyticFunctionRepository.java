/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An in-memory implementation of {@link AnalyticFunctionRepository}.
 * This can either be used as-is through a factory which scans available functions,
 * or it can be used as a cache on top of a more costly function repository. 
 *
 * @author kirk
 */
public class InMemoryAnalyticFunctionRepository implements AnalyticFunctionRepository {
  private final Set<AnalyticFunction> _functions = new HashSet<AnalyticFunction>();
  
  public InMemoryAnalyticFunctionRepository() {
  }
  
  public InMemoryAnalyticFunctionRepository(Collection<? extends AnalyticFunction> functions) {
    if(functions == null) {
      return;
    }
    _functions.addAll(functions);
  }

  @Override
  public Collection<AnalyticFunction> getAllFunctions() {
    return Collections.unmodifiableSet(_functions);
  }

  @Override
  public Collection<AnalyticFunction> getFunctionsProducing(
      Collection<AnalyticValueDefinition<?>> outputs) {
    Set<AnalyticFunction> result = new HashSet<AnalyticFunction>();
    for(AnalyticFunction function : _functions) {
      if(function.getPossibleResults().containsAll(outputs)) {
        result.add(function);
      }
    }
    return result;
  }

  @Override
  public Collection<AnalyticFunction> getFunctionsProducing(
      Collection<AnalyticValueDefinition<?>> outputs, String securityType) {
    Set<AnalyticFunction> result = new HashSet<AnalyticFunction>();
    for(AnalyticFunction function : _functions) {
      if(function.isApplicableTo(securityType)
          && function.getPossibleResults().containsAll(outputs)) {
        result.add(function);
      }
    }
    return result;
  }

}
