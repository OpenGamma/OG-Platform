/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Combines a function repository and compiler to give access to compiled functions.
 */
public class CompiledFunctionService {

  private static final Logger s_logger = LoggerFactory.getLogger(CompiledFunctionService.class);

  private final FunctionRepository _rawFunctionRepository;
  private FunctionRepository _initializedFunctionRepository;
  private final FunctionRepositoryCompiler _functionRepositoryCompiler;
  private final FunctionCompilationContext _functionCompilationContext;
  private Set<FunctionDefinition> _reinitializingFunctionDefinitions;
  private Set<ObjectId> _reinitializingFunctionRequirements;
  private boolean _localExecutorService;
  private ExecutorService _executorService;
  private final FunctionReinitializer _reinitializer = new FunctionReinitializer() {

    @Override
    public synchronized void reinitializeFunction(final FunctionDefinition function, final ObjectId identifier) {
      s_logger.debug("Re-initialize function {} on change to {}", function, identifier);
      _reinitializingFunctionDefinitions.add(function);
      _reinitializingFunctionRequirements.add(identifier);
    }

    @Override
    public synchronized void reinitializeFunction(final FunctionDefinition function, final Collection<ObjectId> identifiers) {
      s_logger.debug("Re-initialize function {} on changes to {}", function, identifiers);
      _reinitializingFunctionDefinitions.add(function);
      _reinitializingFunctionRequirements.addAll(identifiers);
    }

  };

  public CompiledFunctionService(final FunctionRepository functionRepository,
                                 final FunctionRepositoryCompiler functionRepositoryCompiler, final FunctionCompilationContext functionCompilationContext) {
    ArgumentChecker.notNull(functionRepository, "functionRepository");
    ArgumentChecker.notNull(functionRepositoryCompiler, "functionRepositoryCompiler");
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    _rawFunctionRepository = functionRepository;
    _functionRepositoryCompiler = functionRepositoryCompiler;
    _functionCompilationContext = functionCompilationContext;
    _localExecutorService = true;
    _executorService = createDefaultExecutorService();
  }

  protected ExecutorService createDefaultExecutorService() {
    final int processors = Math.max(Runtime.getRuntime().availableProcessors(), 1);
    final ThreadPoolExecutor executorService = new ThreadPoolExecutor(processors, processors, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    executorService.allowCoreThreadTimeOut(true);
    return executorService;
  }

  public void setExecutorService(final ExecutorService executorService) {
    if (_localExecutorService) {
      _executorService.shutdown();
    }
    if (executorService == null) {
      _localExecutorService = true;
      _executorService = createDefaultExecutorService();
    } else {
      _localExecutorService = false;
      _executorService = executorService;
    }
  }

  private static final class StaticFunctionRepository implements FunctionRepository {

    private final Set<FunctionDefinition> _functions;

    private StaticFunctionRepository(final FunctionRepository functions) {
      _functions = new HashSet<FunctionDefinition>((functions != null) ? functions.getAllFunctions() : Collections.<FunctionDefinition>emptyList());
    }

    private void remove(final FunctionDefinition function) {
      _functions.remove(function);
    }

    private void add(final FunctionDefinition function) {
      _functions.add(function);
    }

    @Override
    public Collection<FunctionDefinition> getAllFunctions() {
      return _functions;
    }

  }

  protected void initializeImpl(final long initId, final Collection<FunctionDefinition> functions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Initializing {} function definitions", functions.size());
    final ExecutorCompletionService<FunctionDefinition> completionService = new ExecutorCompletionService<FunctionDefinition>(getExecutorService());
    final int nFunctions = functions.size();
    getFunctionCompilationContext().setFunctionReinitializer(_reinitializer);
    final StaticFunctionRepository initialized = new StaticFunctionRepository(_initializedFunctionRepository);
    for (final FunctionDefinition definition : functions) {
      completionService.submit(new Callable<FunctionDefinition>() {
        @Override
        public FunctionDefinition call() {
          try {
            definition.init(getFunctionCompilationContext());
            return definition;
          } catch (final UnsupportedOperationException e) {
            s_logger.warn("Function {}, is not supported in this configuration - {}", definition.getUniqueId(), e.getMessage());
            s_logger.info("Caught exception", e);
            return null;
          } catch (final Exception e) {
            s_logger.error("Couldn't initialize function {}", definition.getUniqueId());
            throw new OpenGammaRuntimeException("Couldn't initialize " + definition.getShortName(), e);
          }
        }
      });
      initialized.remove(definition);
    }
    for (int i = 0; i < nFunctions; i++) {
      Future<FunctionDefinition> future = null;
      try {
        future = completionService.take();
      } catch (final InterruptedException e1) {
        Thread.interrupted();
        s_logger.warn("Interrupted while initializing function definitions.");
        throw new OpenGammaRuntimeException("Interrupted while initializing function definitions. ViewProcessor not safe to use.");
      }
      try {
        final FunctionDefinition function = future.get();
        if (function != null) {
          initialized.add(function);
        }
      } catch (final Exception e) {
        s_logger.warn("Couldn't initialize function", e);
        // Don't take any further action - the error has been logged and the function is not in the "initialized" set
      }
    }
    _initializedFunctionRepository = initialized;
    getFunctionCompilationContext().setFunctionReinitializer(null);
    getFunctionCompilationContext().setFunctionInitId(initId);
    timer.finished();
  }

  /**
   * Initializes all functions.
   *
   * @return the set of object identifiers that should trigger re-initialization
   */
  public Set<ObjectId> initialize() {
    // If the view processor node has restarted, remote nodes might have old values knocking around. We need a value
    // that won't "accidentally" be the same as theirs. As we increment the ID by 1 each time, the clock is possibly
    // a good choice unless we're clocking config changes at sub-millisecond speeds.
    return initialize(System.currentTimeMillis());
  }

  // NOTE: re-initialization is a bit overzealous at the moment, but is none-the-less an improvement to reinitializing
  // all of the definitions. The reinitialize called by a view processor manager can supply identifiers and only do
  // the required functions, but propagating only the modified identifiers to remote calculation nodes is non-trivial.

  /**
   * Initializes all functions.
   *
   * @param initId the initialization identifier
   * @return the set of object identifiers that should trigger re-initialization
   */
  public synchronized Set<ObjectId> initialize(final long initId) {
    s_logger.info("Initializing all function definitions to {}", initId);
    _reinitializingFunctionDefinitions = new HashSet<FunctionDefinition>();
    _reinitializingFunctionRequirements = new HashSet<ObjectId>();
    initializeImpl(initId, getFunctionRepository().getAllFunctions());
    return _reinitializingFunctionRequirements;
  }

  public synchronized void reinitializeIfNeeded(final long initId) {
    if (getFunctionCompilationContext().getFunctionInitId() != initId) {
      s_logger.info("Re-initializing function definitions - was {} required {}", getFunctionCompilationContext().getFunctionInitId(), initId);
      final Set<FunctionDefinition> reinitialize = _reinitializingFunctionDefinitions;
      if (reinitialize.isEmpty()) {
        s_logger.warn("No functions registered for re-initialization");
        getFunctionCompilationContext().setFunctionInitId(initId);
      } else {
        _reinitializingFunctionDefinitions = new HashSet<FunctionDefinition>();
        _reinitializingFunctionRequirements = new HashSet<ObjectId>();
        initializeImpl(initId, reinitialize);
      }
    }
  }

  /**
   * Re-initializes functions that requested re-initialization during their previous initialization.
   *
   * @return the set of unique identifiers requested by any initialized functions that should trigger re-initialization
   */
  public synchronized Set<ObjectId> reinitialize() {
    final long initId = getFunctionCompilationContext().getFunctionInitId() + 1;
    s_logger.info("Re-initializing all function definitions to {}", initId);
    final Set<FunctionDefinition> reinitialize = _reinitializingFunctionDefinitions;
    if (reinitialize.isEmpty()) {
      s_logger.warn("No functions registered for re-initialization");
      getFunctionCompilationContext().setFunctionInitId(initId);
    } else {
      _reinitializingFunctionDefinitions = new HashSet<FunctionDefinition>();
      _reinitializingFunctionRequirements = new HashSet<ObjectId>();
      initializeImpl(initId, reinitialize);
    }
    return _reinitializingFunctionRequirements;
  }

  /**
   * Returns the underlying (raw) function repository. Definitions in the repository may or may not be properly initialized. If
   * functions are needed that can be reliably used, use {@link #getInitializedFunctionRepository} instead.
   *
   * @return the function repository, not null
   */
  public FunctionRepository getFunctionRepository() {
    return _rawFunctionRepository;
  }

  /**
   * Returns a repository of initialized functions. This may be a subset of the underlying (raw) repository if one or more threw
   * exceptions during their {@link FunctionDefinition#init} calls.
   *
   * @return the function repository, not null
   */
  public synchronized FunctionRepository getInitializedFunctionRepository() {
    return _initializedFunctionRepository;
  }

  public FunctionRepositoryCompiler getFunctionRepositoryCompiler() {
    return _functionRepositoryCompiler;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  public CompiledFunctionRepository compileFunctionRepository(final long timestamp) {
    return getFunctionRepositoryCompiler().compile(getInitializedFunctionRepository(), getFunctionCompilationContext(), getExecutorService(), Instant.ofEpochMilli(timestamp));
  }

  public CompiledFunctionRepository compileFunctionRepository(final Instant timestamp) {
    return getFunctionRepositoryCompiler().compile(getInitializedFunctionRepository(), getFunctionCompilationContext(), getExecutorService(), timestamp);
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  @Override
  public CompiledFunctionService clone() {
    return new CompiledFunctionService(getFunctionRepository(), getFunctionRepositoryCompiler(), getFunctionCompilationContext().clone());
  }

}
