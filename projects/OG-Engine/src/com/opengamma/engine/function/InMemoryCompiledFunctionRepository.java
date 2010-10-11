/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory implementation of {@link CompiledFunctionRepository}.
 */
public class InMemoryCompiledFunctionRepository implements CompiledFunctionRepository {

  private final Map<String, CompiledFunctionDefinition> _functionDefinitions = new ConcurrentHashMap<String, CompiledFunctionDefinition>();
  private final Map<String, FunctionInvoker> _functionInvokers = new ConcurrentHashMap<String, FunctionInvoker>();
  private final FunctionCompilationContext _compilationContext;

  public InMemoryCompiledFunctionRepository(final FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }

  public void addFunction(CompiledFunctionDefinition function) {
    ArgumentChecker.notNull(function, "Function definition");
    final String uid = function.getFunctionDefinition().getUniqueIdentifier();
    _functionDefinitions.put(uid, function);
    _functionInvokers.put(uid, function.getFunctionInvoker());
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
  public FunctionInvoker getInvoker(String uniqueIdentifier) {
    return _functionInvokers.get(uniqueIdentifier);
  }

  @Override
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

}
