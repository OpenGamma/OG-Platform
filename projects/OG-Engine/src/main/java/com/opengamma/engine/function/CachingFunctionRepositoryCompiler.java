/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Implements a {@link FunctionRepositoryCompiler} that caches the results of previous compilations so that a minimal compilation is performed each time.
 */
public class CachingFunctionRepositoryCompiler implements FunctionRepositoryCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(CachingFunctionRepositoryCompiler.class);

  private static final Comparator<Pair<FunctionRepository, Instant>> s_comparator = new Comparator<Pair<FunctionRepository, Instant>>() {
    @Override
    public int compare(final Pair<FunctionRepository, Instant> o1, final Pair<FunctionRepository, Instant> o2) {
      if (o1 == o2) {
        return 0;
      }
      if (o1.getFirst() == o2.getFirst()) {
        // Same repository, order by timestamp
        return o1.getSecond().compareTo(o2.getSecond());
      }
      // If the repositories aren't equal, just need a deterministic ordering
      final int hc1 = o1.getFirst().hashCode();
      final int hc2 = o2.getFirst().hashCode();
      if (hc1 < hc2) {
        return -1;
      }
      if (hc1 > hc2) {
        return 1;
      }
      return o1.getSecond().compareTo(o2.getSecond());
    }
  };
  private final TreeMap<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> _compilationCache = new TreeMap<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository>(
      s_comparator);
  private final Queue<Pair<FunctionRepository, Instant>> _activeEntries = new ArrayDeque<Pair<FunctionRepository, Instant>>();
  private int _cacheSize = 16;
  private long _functionInitId;
  private final ConcurrentMap<Pair<FunctionRepository, Instant>, Callable<CompiledFunctionRepository>> _activeCompilations =
      new ConcurrentHashMap<Pair<FunctionRepository, Instant>, Callable<CompiledFunctionRepository>>();

  public synchronized void setCacheSize(final int cacheSize) {
    _cacheSize = cacheSize;
    while (_activeEntries.size() > cacheSize) {
      _compilationCache.remove(_activeEntries.remove());
    }
  }

  public int getCacheSize() {
    return _cacheSize;
  }

  protected TreeMap<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> getCompilationCache() {
    return _compilationCache;
  }

  protected Queue<Pair<FunctionRepository, Instant>> getActiveCacheEntries() {
    return _activeEntries;
  }

  protected boolean addFunctionFromCachedRepository(final InMemoryCompiledFunctionRepository before, final InMemoryCompiledFunctionRepository after, final InMemoryCompiledFunctionRepository compiled,
      final FunctionDefinition function, final Instant atInstant) {
    if (before != null) {
      final CompiledFunctionDefinition compiledFunction = before.findDefinition(function.getUniqueId());
      if (compiledFunction != null) {
        if (compiledFunction.getLatestInvocationTime() == null) {
          // previous one always valid
          compiled.addFunction(compiledFunction);
          return true;
        } else {
          final Instant validUntil = compiledFunction.getLatestInvocationTime();
          if (!validUntil.isBefore(atInstant)) {
            // previous one still valid
            compiled.addFunction(compiledFunction);
            return true;
          }
        }
      }
    }
    if (after != null) {
      final CompiledFunctionDefinition compiledFunction = after.findDefinition(function.getUniqueId());
      if (compiledFunction != null) {
        if (compiledFunction.getEarliestInvocationTime() == null) {
          // next one always valid
          compiled.addFunction(compiledFunction);
          return true;
        } else {
          final Instant validFrom = compiledFunction.getEarliestInvocationTime();
          if (!validFrom.isAfter(atInstant)) {
            // next one already valid
            compiled.addFunction(compiledFunction);
            return true;
          }
        }
      }
    }
    return false;
  }

  protected InMemoryCompiledFunctionRepository compile(final FunctionCompilationContext context, final FunctionRepository functions, final Instant atInstant,
      final InMemoryCompiledFunctionRepository before, final InMemoryCompiledFunctionRepository after, final PoolExecutor poolExecutor) {
    final InMemoryCompiledFunctionRepository compiled = new InMemoryCompiledFunctionRepository(context);
    final AtomicInteger failures = new AtomicInteger();
    final PoolExecutor.Service<CompiledFunctionDefinition> jobs = poolExecutor.createService(new CompletionListener<CompiledFunctionDefinition>() {

      @Override
      public void success(final CompiledFunctionDefinition result) {
        compiled.addFunction(result);
      }

      @Override
      public void failure(final Throwable error) {
        // Don't propagate the error outwards; it just won't be in the compiled repository
        s_logger.debug("Error compiling function definition", error);
        failures.incrementAndGet();
      }

    });
    for (final FunctionDefinition function : functions.getAllFunctions()) {
      if (addFunctionFromCachedRepository(before, after, compiled, function, atInstant)) {
        continue;
      }
      jobs.execute(new Callable<CompiledFunctionDefinition>() {
        @Override
        public CompiledFunctionDefinition call() throws Exception {
          try {
            s_logger.debug("Compiling {}", function);
            return function.compile(context, atInstant);
          } catch (final Exception e) {
            s_logger.warn("Compiling {} threw {}", function.getShortName(), e);
            throw e;
          }
        }
      });
    }
    try {
      jobs.join();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Interrupted while compiling function definitions.");
    }
    if (failures.get() != 0) {
      s_logger.error("Encountered {} errors while compiling repository", failures);
    }
    return compiled;
  }

  protected synchronized InMemoryCompiledFunctionRepository getCachedCompilation(final Pair<FunctionRepository, Instant> key) {
    return getCompilationCache().get(key);
  }

  protected synchronized InMemoryCompiledFunctionRepository getPreviousCompilation(final Pair<FunctionRepository, Instant> key) {
    final Map.Entry<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> entry = getCompilationCache().lowerEntry(key);
    if ((entry != null) && (entry.getKey().getFirst() == key.getFirst())) {
      return entry.getValue();
    }
    return null;
  }

  protected synchronized InMemoryCompiledFunctionRepository getNextCompilation(final Pair<FunctionRepository, Instant> key) {
    final Map.Entry<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> entry = getCompilationCache().higherEntry(key);
    if ((entry != null) && (entry.getKey().getFirst() == key.getFirst())) {
      return entry.getValue();
    }
    return null;
  }

  protected synchronized void cacheCompilation(final Pair<FunctionRepository, Instant> key, final InMemoryCompiledFunctionRepository compiled) {
    final Queue<Pair<FunctionRepository, Instant>> active = getActiveCacheEntries();
    if (active.size() >= getCacheSize()) {
      getCompilationCache().remove(active.remove());
    }
    getCompilationCache().put(key, compiled);
    getActiveCacheEntries().add(key);
  }

  @Override
  public CompiledFunctionRepository compile(final FunctionRepository repository, final FunctionCompilationContext context, final PoolExecutor executor, final Instant atInstant) {
    clearInvalidCache(context.getFunctionInitId());
    final Pair<FunctionRepository, Instant> key = Pairs.of(repository, atInstant);
    // Try a previous compilation
    final InMemoryCompiledFunctionRepository previous = getPreviousCompilation(key);
    if (previous != null) {
      if (previous.getLatestInvocationTime() == null) {
        return previous;
      } else {
        if (!atInstant.isAfter(previous.getLatestInvocationTime())) {
          return previous;
        }
      }
    }
    // Try a future compilation
    final InMemoryCompiledFunctionRepository next = getNextCompilation(key);
    if (next != null) {
      if (next.getEarliestInvocationTime() == null) {
        return next;
      } else {
        if (!atInstant.isBefore(next.getEarliestInvocationTime())) {
          return next;
        }
      }
    }
    // Try the exact timestamp
    InMemoryCompiledFunctionRepository compiled = getCachedCompilation(key);
    if (compiled != null) {
      return compiled;
    }
    // Create a compilation, salvaging results from previous and next if possible
    final CompiledFunctionRepository[] ref = new CompiledFunctionRepository[1];
    final Callable<CompiledFunctionRepository> existing = _activeCompilations.putIfAbsent(key, new Callable<CompiledFunctionRepository>() {
      @Override
      public CompiledFunctionRepository call() throws Exception {
        CompiledFunctionRepository repository;
        synchronized (ref) {
          repository = ref[0];
          while (repository == null) {
            s_logger.info("Waiting for concurrent call to compile {}", atInstant);
            ref.wait();
            repository = ref[0];
          }
        }
        return repository;
      }
    });
    if (existing != null) {
      try {
        s_logger.info("Using concurrent call to compile {}", atInstant);
        return existing.call();
      } catch (final Exception e) {
        throw new OpenGammaRuntimeException("Exception from concurrent call", e);
      }
    }
    compiled = compile(context, repository, atInstant, previous, next, executor);
    cacheCompilation(key, compiled);
    synchronized (ref) {
      ref[0] = compiled;
      ref.notifyAll();
    }
    _activeCompilations.remove(key);
    return compiled;
  }

  protected synchronized void clearInvalidCache(final Long initId) {
    if ((initId != null) && (_functionInitId != initId)) {
      getCompilationCache().clear();
      _functionInitId = initId;
    }
  }

}
