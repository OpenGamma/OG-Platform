/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.Instant;

import com.opengamma.util.PoolExecutor;

/**
 * Defers the compilation of any functions until the definitions are requested. This may be useful for remote calculation nodes to only compile functions that a node needs. It should not be used by a
 * view processor for dependency graph compilation as not all function definitions may be available.
 */
public class LazyFunctionRepositoryCompiler extends CachingFunctionRepositoryCompiler {

  private static class Repository extends InMemoryCompiledFunctionRepository {

    private final ConcurrentMap<String, FunctionDefinition> _uncompiled = new ConcurrentHashMap<String, FunctionDefinition>();
    private final Instant _atInstant;

    public Repository(final FunctionCompilationContext functionCompilationContext, final Instant atInstant) {
      super(functionCompilationContext);
      _atInstant = atInstant;
    }

    @Override
    public CompiledFunctionDefinition getDefinition(final String uniqueId) {
      FunctionDefinition function = _uncompiled.get(uniqueId);
      if (function != null) {
        synchronized (this) {
          function = _uncompiled.get(uniqueId);
          if (function != null) {
            final CompiledFunctionDefinition compiled = function.compile(getCompilationContext(), _atInstant);
            addFunction(compiled);
            _uncompiled.remove(uniqueId);
          }
        }
      }
      return super.getDefinition(uniqueId);
    }

    public void addUncompiledFunction(final FunctionDefinition function) {
      _uncompiled.put(function.getUniqueId(), function);
    }

    @Override
    public Instant getEarliestInvocationTime() {
      return _uncompiled.isEmpty() ? super.getEarliestInvocationTime() : _atInstant;
    }

    @Override
    public Instant getLatestInvocationTime() {
      return _uncompiled.isEmpty() ? super.getLatestInvocationTime() : _atInstant;
    }

  }

  @Override
  protected InMemoryCompiledFunctionRepository compile(final FunctionCompilationContext context, final FunctionRepository functions, final Instant atInstant,
      final InMemoryCompiledFunctionRepository before, final InMemoryCompiledFunctionRepository after, final PoolExecutor executorService) {
    final Repository compiled = new Repository(context, atInstant);
    for (final FunctionDefinition function : functions.getAllFunctions()) {
      if (addFunctionFromCachedRepository(before, after, compiled, function, atInstant)) {
        continue;
      }
      compiled.addUncompiledFunction(function);
    }
    return compiled;
  }

}
