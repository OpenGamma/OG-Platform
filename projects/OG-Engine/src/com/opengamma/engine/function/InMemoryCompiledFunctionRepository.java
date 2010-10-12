/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory implementation of {@link CompiledFunctionRepository}.
 */
public class InMemoryCompiledFunctionRepository implements CompiledFunctionRepository {

  private static final FunctionInvoker MISSING = new FunctionInvoker() {
    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
      return null;
    }
  };

  private final ConcurrentMap<String, CompiledFunctionDefinition> _functionDefinitions = new ConcurrentHashMap<String, CompiledFunctionDefinition>();
  private final ConcurrentMap<String, FunctionInvoker> _functionInvokers = new ConcurrentHashMap<String, FunctionInvoker>();
  private final FunctionCompilationContext _compilationContext;

  public InMemoryCompiledFunctionRepository(final FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }

  public void addFunction(CompiledFunctionDefinition function) {
    ArgumentChecker.notNull(function, "Function definition");
    final String uid = function.getFunctionDefinition().getUniqueIdentifier();
    _functionDefinitions.put(uid, function);
  }

  @Override
  public Collection<CompiledFunctionDefinition> getAllFunctions() {
    return _functionDefinitions.values();
  }

  @Override
  public CompiledFunctionDefinition getDefinition(final String uniqueIdentifier) {
    return _functionDefinitions.get(uniqueIdentifier);
  }

  @Override
  public FunctionInvoker getInvoker(final String uniqueIdentifier) {
    FunctionInvoker invoker = _functionInvokers.get(uniqueIdentifier);
    if (invoker == null) {
      final CompiledFunctionDefinition definition = getDefinition(uniqueIdentifier);
      if (definition == null) {
        invoker = MISSING;
      } else {
        invoker = definition.getFunctionInvoker();
      }
      final FunctionInvoker previous = _functionInvokers.putIfAbsent(uniqueIdentifier, invoker);
      if (previous != null) {
        invoker = previous;
      }
    }
    return (invoker == MISSING) ? null : invoker;
  }

  @Override
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

}
