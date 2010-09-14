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

import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory implementation of {@link FunctionRepository}.
 * This can either be used as-is through a factory which scans available functions,
 * or it can be used as a cache on top of a more costly function repository. 
 *
 */
public class InMemoryFunctionRepository implements FunctionRepository {
  private final Map<String, FunctionInvoker> _invokersByUniqueIdentifier =
    new HashMap<String, FunctionInvoker>();
  private final Set<FunctionDefinition> _functions = new HashSet<FunctionDefinition>();
  
  public InMemoryFunctionRepository() {
  }
  
  public synchronized void addFunction(AbstractFunction function, FunctionInvoker invoker) {
    ArgumentChecker.notNull(function, "Function definition");
    ArgumentChecker.notNull(invoker, "Function invoker");
    _functions.add(function);
    if (function.getUniqueIdentifier() == null) {
      function.setUniqueIdentifier(Integer.toString(_functions.size()));
    }
    _invokersByUniqueIdentifier.put(function.getUniqueIdentifier(), invoker);
  }

  @Override
  public Collection<FunctionDefinition> getAllFunctions() {
    return Collections.unmodifiableCollection(_functions);
  }

  @Override
  public FunctionInvoker getInvoker(String uniqueIdentifier) {
    return _invokersByUniqueIdentifier.get(uniqueIdentifier);
  }
  
  /* Temporary method so we can swap in new versions of date-specific curve functions until the engine can do it */
  public void replace(AbstractFunction originalFunction, AbstractFunction replacementFunction) {
    _functions.remove(originalFunction);
    _functions.add(replacementFunction);
    replacementFunction.setUniqueIdentifier(originalFunction.getUniqueIdentifier());
    _invokersByUniqueIdentifier.put(originalFunction.getUniqueIdentifier(), (FunctionInvoker) replacementFunction);
  }

  /**
   * This method is primarily useful for testing, as otherwise it will be
   * done explicitly by the {@link ViewProcessor} on startup.
   * 
   * @param compilationContext The context to provide to each function.
   */
  public void initFunctions(FunctionCompilationContext compilationContext) {
    for (FunctionDefinition function : _functions) {
      function.init(compilationContext);
    }
  }

}
