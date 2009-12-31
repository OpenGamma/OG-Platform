/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory implementation of {@link FunctionRepository}.
 * This can either be used as-is through a factory which scans available functions,
 * or it can be used as a cache on top of a more costly function repository. 
 *
 * @author kirk
 */
public class InMemoryFunctionRepository implements FunctionRepository {
  private final Map<String, FunctionInvoker> _invokersByUniqueIdentifier =
    new HashMap<String, FunctionInvoker>();
  private final Set<FunctionDefinition> _functions = new HashSet<FunctionDefinition>();
  
  public InMemoryFunctionRepository() {
  }
  
  public synchronized void addFunction(AbstractFunction function, FunctionInvoker invoker) {
    validateFunction(function, invoker);
    _functions.add(function);
    function.setUniqueIdentifier(Integer.toString(_functions.size()));
    _invokersByUniqueIdentifier.put(function.getUniqueIdentifier(), invoker);
  }

  /**
   * @param function
   * @param invoker
   */
  private void validateFunction(AbstractFunction function,
      FunctionInvoker invoker) {
    // REVIEW kirk 2009-12-31 -- After the rewrite is done this is meaningless.
    if(function == null) {
      throw new NullPointerException("Must provide a function.");
    }
    if(invoker == null) {
      throw new NullPointerException("Must provide an invoker.");
    }
    if(function instanceof NewFunctionDefinition) {
      if(!(invoker instanceof NewFunctionInvoker)) {
        throw new IllegalArgumentException("Must provide new style invoker for aggregate position definition.");
      }
    } else {
      throw new IllegalArgumentException("Unexpected analytic function definition " + function.getClass());
    }
  }

  @Override
  public Collection<FunctionDefinition> getAllFunctions() {
    return Collections.unmodifiableCollection(_functions);
  }

  @Override
  public FunctionInvoker getInvoker(String uniqueIdentifier) {
    return _invokersByUniqueIdentifier.get(uniqueIdentifier);
  }

}
