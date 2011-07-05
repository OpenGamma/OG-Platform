/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Combines a function repository and compiler to give access to compiled functions. 
 */
public class CompiledFunctionService {

  private static final Logger s_logger = LoggerFactory.getLogger(CompiledFunctionService.class);

  private final FunctionRepository _functionRepository;
  private final FunctionRepositoryCompiler _functionRepositoryCompiler;
  private final FunctionCompilationContext _functionCompilationContext;
  private Set<FunctionDefinition> _reinitializingFunctionDefinitions;
  private Set<UniqueIdentifier> _reinitializingFunctionRequirements;
  private boolean _localExecutorService;
  private ExecutorService _executorService;
  private final FunctionReinitializer _reinitializer = new FunctionReinitializer() {

    @Override
    public synchronized void reinitializeFunction(FunctionDefinition function, UniqueIdentifier identifier) {
      s_logger.debug("Re-initialize function {} on change to {}", function, identifier);
      _reinitializingFunctionDefinitions.add(function);
      _reinitializingFunctionRequirements.add(identifier);
    }

    @Override
    public synchronized void reinitializeFunction(FunctionDefinition function, Collection<UniqueIdentifier> identifiers) {
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
    _functionRepository = functionRepository;
    _functionRepositoryCompiler = functionRepositoryCompiler;
    _functionCompilationContext = functionCompilationContext;
    _localExecutorService = true;
    _executorService = createDefaultExecutorService();
  }

  protected ExecutorService createDefaultExecutorService() {
    return new ThreadPoolExecutor(1, Math.max(Runtime.getRuntime().availableProcessors(), 1), 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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

  protected void initializeImpl(final long initId, final Collection<FunctionDefinition> functions) {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing {} function definitions", functions.size());
    // TODO kirk 2010-03-07 -- Better error handling.
    final ExecutorCompletionService<FunctionDefinition> completionService = new ExecutorCompletionService<FunctionDefinition>(getExecutorService());
    int nFunctions = functions.size();
    getFunctionCompilationContext().setFunctionReinitializer(_reinitializer);
    for (final FunctionDefinition definition : functions) {
      completionService.submit(new Runnable() {
        @Override
        public void run() {
          definition.init(getFunctionCompilationContext());
        }
      }, definition);
    }
    for (int i = 0; i < nFunctions; i++) {
      Future<FunctionDefinition> future = null;
      try {
        future = completionService.take();
      } catch (InterruptedException e1) {
        Thread.interrupted();
        s_logger.warn("Interrupted while initializing function definitions.");
        throw new OpenGammaRuntimeException("Interrupted while initializing function definitions. ViewProcessor not safe to use.");
      }
      try {
        future.get();
      } catch (Exception e) {
        s_logger.warn("Got exception check back on future for initializing FunctionDefinition. See above log entries", e);
        throw new OpenGammaRuntimeException("Couldn't initialize function", e);
      }
    }
    getFunctionCompilationContext().setFunctionReinitializer(null);
    getFunctionCompilationContext().setFunctionInitId(initId);
    timer.finished();
  }

  /**
   * Initializes all functions.
   * 
   * @return the set of unique identifiers that should trigger re-initialization
   */
  public Set<UniqueIdentifier> initialize() {
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
   * @return the set of unique identifiers that should trigger re-initialization
   */
  public synchronized Set<UniqueIdentifier> initialize(final long initId) {
    s_logger.info("Initializing all function definitions to {}", initId);
    _reinitializingFunctionDefinitions = new HashSet<FunctionDefinition>();
    _reinitializingFunctionRequirements = new HashSet<UniqueIdentifier>();
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
        _reinitializingFunctionRequirements = new HashSet<UniqueIdentifier>();
        initializeImpl(initId, reinitialize);
      }
    }
  }

  /**
   * Re-initializes functions that requested re-initialization during their previous initialization.
   * 
   * @return the set of unique identifiers requested by any initialized functions that should trigger re-initialization
   */
  public synchronized Set<UniqueIdentifier> reinitialize() {
    long initId = getFunctionCompilationContext().getFunctionInitId() + 1;
    s_logger.info("Re-initializing all function definitions to {}", initId);
    final Set<FunctionDefinition> reinitialize = _reinitializingFunctionDefinitions;
    if (reinitialize.isEmpty()) {
      s_logger.warn("No functions registered for re-initialization");
      getFunctionCompilationContext().setFunctionInitId(initId);
    } else {
      _reinitializingFunctionDefinitions = new HashSet<FunctionDefinition>();
      _reinitializingFunctionRequirements = new HashSet<UniqueIdentifier>();
      initializeImpl(initId, reinitialize);
    }
    return _reinitializingFunctionRequirements;
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public FunctionRepositoryCompiler getFunctionRepositoryCompiler() {
    return _functionRepositoryCompiler;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  public CompiledFunctionRepository compileFunctionRepository(final long timestamp) {
    return getFunctionRepositoryCompiler().compile(getFunctionRepository(), getFunctionCompilationContext(), getExecutorService(), Instant.ofEpochMillis(timestamp));
  }

  public CompiledFunctionRepository compileFunctionRepository(final InstantProvider timestamp) {
    return getFunctionRepositoryCompiler().compile(getFunctionRepository(), getFunctionCompilationContext(), getExecutorService(), timestamp);
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public CompiledFunctionService clone() {
    return new CompiledFunctionService(getFunctionRepository(), getFunctionRepositoryCompiler(), getFunctionCompilationContext().clone());
  }

}
