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

import com.opengamma.engine.security.Security;

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
  private final Set<AnalyticFunctionDefinition> _functions = new HashSet<AnalyticFunctionDefinition>();
  
  public InMemoryAnalyticFunctionRepository() {
  }
  
  public synchronized void addFunction(AbstractAnalyticFunction function, AnalyticFunctionInvoker invoker) {
    validateFunction(function, invoker);
    _functions.add(function);
    function.setUniqueIdentifier(Integer.toString(_functions.size()));
    _invokersByUniqueIdentifier.put(function.getUniqueIdentifier(), invoker);
  }

  /**
   * @param function
   * @param invoker
   */
  private void validateFunction(AbstractAnalyticFunction function,
      AnalyticFunctionInvoker invoker) {
    if(function == null) {
      throw new NullPointerException("Must provide a function.");
    }
    if(invoker == null) {
      throw new NullPointerException("Must provide an invoker.");
    }
    if(function instanceof PrimitiveAnalyticFunctionDefinition) {
      if(!(invoker instanceof PrimitiveAnalyticFunctionInvoker)) {
        throw new IllegalArgumentException("Must provide primitive invoker for primitive definition.");
      }
    } else if(function instanceof SecurityAnalyticFunctionDefinition) {
      if(!(invoker instanceof SecurityAnalyticFunctionInvoker)) {
        throw new IllegalArgumentException("Must provide security invoker for security definition.");
      }
    } else if(function instanceof PositionAnalyticFunctionDefinition) {
      throw new UnsupportedOperationException("Cannot currently handle PositionAnalyticFunctionInvokers, as they've not been written.");
    } else {
      throw new IllegalArgumentException("Unexpected analytic function definition " + function.getClass());
    }
  }

  @Override
  public Collection<AnalyticFunctionDefinition> getAllFunctions() {
    return Collections.unmodifiableCollection(_functions);
  }

  @Override
  public Collection<AnalyticFunctionDefinition> getFunctionsProducing(
      Collection<AnalyticValueDefinition<?>> outputs) {
    Set<AnalyticFunctionDefinition> result = new HashSet<AnalyticFunctionDefinition>();
    for(AnalyticFunctionDefinition function : _functions) {
      if(!(function instanceof PrimitiveAnalyticFunctionDefinition)) {
        // Can't work with it because it requires execution inside of a
        // security or position context.
        continue;
      }
      if(functionProducesAllValues(function, null, outputs)) {
        result.add(function);
      }
    }
    return result;
  }

  @Override
  public Collection<AnalyticFunctionDefinition> getFunctionsProducing(
      Collection<AnalyticValueDefinition<?>> outputs, Security security) {
    Set<AnalyticFunctionDefinition> result = new HashSet<AnalyticFunctionDefinition>();
    for(AnalyticFunctionDefinition function : _functions) {
      boolean applicable = true;
      if(function instanceof PrimitiveAnalyticFunctionDefinition) {
        // Might be applicable even if we're in a security-specific context.
        applicable = true;
      } else if(function instanceof SecurityAnalyticFunctionDefinition) {
        SecurityAnalyticFunctionDefinition secDefinition = (SecurityAnalyticFunctionDefinition) function;
        if(security != null) {
          applicable = secDefinition.isApplicableTo(security.getSecurityType());
        } else {
          applicable = secDefinition.isApplicableTo((String)null);
        }
      }
      if(applicable
          && functionProducesAllValues(function, security, outputs)) {
        result.add(function);
      }
    }
    return result;
  }
  
  protected boolean functionProducesAllValues(
      AnalyticFunctionDefinition function,
      Security security,
      Collection<AnalyticValueDefinition<?>> outputs) {
    Collection<AnalyticValueDefinition<?>> possibleResults = null;
    if(function instanceof PrimitiveAnalyticFunctionDefinition) {
      possibleResults = ((PrimitiveAnalyticFunctionDefinition) function).getPossibleResults();
    } else if(function instanceof SecurityAnalyticFunctionDefinition) {
      possibleResults = ((SecurityAnalyticFunctionDefinition) function).getPossibleResults(security);
    } else {
      throw new UnsupportedOperationException("Can only handle primitive/security functions now.");
    }
    assert possibleResults != null;
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
