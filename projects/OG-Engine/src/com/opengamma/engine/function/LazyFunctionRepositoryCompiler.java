/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import javax.time.Instant;

/**
 * Defers the compilation of any functions until the definitions are requested. This may be useful for
 * remote calculation nodes to only compile functions that a node needs. It should not be used by a
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
    public CompiledFunctionDefinition getDefinition(final String uniqueIdentifier) {
      final FunctionDefinition function = _uncompiled.get(uniqueIdentifier);
      if (function != null) {
        final CompiledFunctionDefinition compiled = function.compile(getCompilationContext(), _atInstant);
        addFunction(compiled);
        _uncompiled.remove(uniqueIdentifier);
      }
      return super.getDefinition(uniqueIdentifier);
    }

    public void addUncompiledFunction(final FunctionDefinition function) {
      _uncompiled.put(function.getUniqueIdentifier(), function);
    }

  }

  @Override
  protected CompiledFunctionRepository compile(final FunctionCompilationContext context, final FunctionRepository functions, final Instant atInstant, final CompiledFunctionRepository before,
      final CompiledFunctionRepository after, final ExecutorService executorService) {
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
