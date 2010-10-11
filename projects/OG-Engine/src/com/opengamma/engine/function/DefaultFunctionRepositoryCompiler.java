/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.util.tuple.Pair;

/**
 * Implements a {@link FunctionRepositoryCompiler} that caches the results of previous compilations so
 * that a minimal compilation is performed each time. 
 */
public class DefaultFunctionRepositoryCompiler implements FunctionRepositoryCompiler {

  private final TreeMap<Pair<FunctionRepository, Instant>, CompiledFunctionRepository> _compilationCache = new TreeMap<Pair<FunctionRepository, Instant>, CompiledFunctionRepository>();
  private final Queue<Pair<FunctionRepository, Instant>> _activeEntries = new ArrayDeque<Pair<FunctionRepository, Instant>>();
  private int _cacheSize = 16;

  public synchronized void setCacheSize(final int cacheSize) {
    _cacheSize = cacheSize;
    while (_activeEntries.size() > cacheSize) {
      _compilationCache.remove(_activeEntries.remove());
    }
  }

  public int getCacheSize() {
    return _cacheSize;
  }

  protected TreeMap<Pair<FunctionRepository, Instant>, CompiledFunctionRepository> getCompilationCache() {
    return _compilationCache;
  }

  protected Queue<Pair<FunctionRepository, Instant>> getActiveCacheEntries() {
    return _activeEntries;
  }

  protected CompiledFunctionRepository compile(final FunctionCompilationContext context, final FunctionRepository functions, final Instant atInstant, final CompiledFunctionRepository before,
      final CompiledFunctionRepository after) {
    final InMemoryCompiledFunctionRepository compiled = new InMemoryCompiledFunctionRepository(context);
    for (FunctionDefinition function : functions.getAllFunctions()) {
      if (before != null) {
        final CompiledFunctionDefinition compiledFunction = before.getDefinition(function.getUniqueIdentifier());
        if (compiledFunction.getLatestInvocationTime() == null) {
          // previous one always valid
          compiled.addFunction(compiledFunction);
          continue;
        } else {
          final Instant validUntil = Instant.of(compiledFunction.getLatestInvocationTime());
          if (!validUntil.isBefore(atInstant)) {
            // previous one still valid
            compiled.addFunction(compiledFunction);
            continue;
          }
        }
      }
      if (after != null) {
        final CompiledFunctionDefinition compiledFunction = after.getDefinition(function.getUniqueIdentifier());
        if (compiledFunction.getEarliestInvocationTime() == null) {
          // next one always valid
          compiled.addFunction(compiledFunction);
          continue;
        } else {
          final Instant validFrom = Instant.of(compiledFunction.getEarliestInvocationTime());
          if (!validFrom.isAfter(atInstant)) {
            // next one already valid
            compiled.addFunction(compiledFunction);
            continue;
          }
        }
      }
      compiled.addFunction(function.compile(context, atInstant));
    }
    return compiled;
  }

  protected CompiledFunctionRepository getCachedCompilation(final Pair<FunctionRepository, Instant> key) {
    return getCompilationCache().get(key);
  }

  protected CompiledFunctionRepository getPreviousCompilation(final Pair<FunctionRepository, Instant> key) {
    final Map.Entry<Pair<FunctionRepository, Instant>, CompiledFunctionRepository> entry = getCompilationCache().lowerEntry(key);
    if ((entry != null) && (entry.getKey().getFirst() == key.getFirst())) {
      return entry.getValue();
    }
    return null;
  }

  protected CompiledFunctionRepository getNextCompilation(final Pair<FunctionRepository, Instant> key) {
    final Map.Entry<Pair<FunctionRepository, Instant>, CompiledFunctionRepository> entry = getCompilationCache().higherEntry(key);
    if ((entry != null) && (entry.getKey().getFirst() == key.getFirst())) {
      return entry.getValue();
    }
    return null;
  }

  protected void cacheCompilation(final Pair<FunctionRepository, Instant> key, final CompiledFunctionRepository compiled) {
    final Queue<Pair<FunctionRepository, Instant>> active = getActiveCacheEntries();
    if (active.size() >= getCacheSize()) {
      getCompilationCache().remove(active.remove());
    }
    getCompilationCache().put(key, compiled);
    getActiveCacheEntries().add(key);
  }

  @Override
  public synchronized CompiledFunctionRepository compile(final FunctionCompilationContext context, final FunctionRepository functions, final InstantProvider atInstantProvider) {
    final Instant atInstant = Instant.of(atInstantProvider);
    final Pair<FunctionRepository, Instant> key = Pair.of(functions, atInstant);
    CompiledFunctionRepository compiled = getCachedCompilation(key);
    if (compiled == null) {
      compiled = compile(context, functions, atInstant, getPreviousCompilation(key), getNextCompilation(key));
      cacheCompilation(key, compiled);
    }
    return compiled;
  }

}
