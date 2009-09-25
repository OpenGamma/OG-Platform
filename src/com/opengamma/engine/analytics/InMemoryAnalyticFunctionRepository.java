/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory implementation of {@link AnalyticFunctionRepository}.
 * This can either be used as-is through a factory which scans available functions,
 * or it can be used as a cache on top of a more costly function repository. 
 *
 * @author kirk
 */
public class InMemoryAnalyticFunctionRepository implements AnalyticFunctionRepository {
  private final Map<String, AnalyticFunctionInvoker> _invokersByUniqueIdentifier =
    new HashMap<String, AnalyticFunctionInvoker>();
  private final Set<AnalyticFunction> _functions = new HashSet<AnalyticFunction>();
  
  public InMemoryAnalyticFunctionRepository() {
  }
  
  public InMemoryAnalyticFunctionRepository(Collection<? extends AbstractAnalyticFunction> functions) {
    addFunctions(functions);
  }
  
  public void addFunctions(Collection<? extends AbstractAnalyticFunction> functions) {
    if(functions == null) {
      return;
    }
    for(AbstractAnalyticFunction function : functions) {
      addFunction(function);
    }
  }
  
  public synchronized void addFunction(AbstractAnalyticFunction function) {
    if(function == null) {
      throw new NullPointerException("Must provide a function.");
    }
    _functions.add(function);
    function.setUniqueIdentifier(Integer.toString(_functions.size()));
    _invokersByUniqueIdentifier.put(function.getUniqueIdentifier(), function);
  }

  @Override
  public Collection<AnalyticFunction> getAllFunctions() {
    return Collections.unmodifiableCollection(_functions);
  }

  @Override
  public Collection<AnalyticFunction> getFunctionsProducing(
      Collection<AnalyticValueDefinition<?>> outputs) {
    Set<AnalyticFunction> result = new HashSet<AnalyticFunction>();
    for(AnalyticFunction function : _functions) {
      if(functionProducesAllValues(function, outputs)) {
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
          && functionProducesAllValues(function, outputs)) {
        result.add(function);
      }
    }
    return result;
  }
  
  protected boolean functionProducesAllValues(AnalyticFunction function, Collection<AnalyticValueDefinition<?>> outputs) {
    Collection<AnalyticValueDefinition<?>> possibleResults = function.getPossibleResults();
    for(AnalyticValueDefinition<?> output : outputs) {
      boolean foundForOutput = false;
      for(AnalyticValueDefinition<?> possibleResult : possibleResults) {
        if(AnalyticValueDefinitionComparator.matches(output, possibleResult)) {
          foundForOutput = true;
          break;
        }
      }
      if(!foundForOutput) {
        return false;
      }
    }
    return true;
  }

  @Override
  public AnalyticFunctionInvoker getInvoker(String uniqueIdentifier) {
    return _invokersByUniqueIdentifier.get(uniqueIdentifier);
  }

}
