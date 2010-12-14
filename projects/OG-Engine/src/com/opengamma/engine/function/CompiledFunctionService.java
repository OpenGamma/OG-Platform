/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

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
  private boolean _localExecutorService;
  private ExecutorService _executorService;

  public CompiledFunctionService(final FunctionRepository functionRepository, final FunctionRepositoryCompiler functionRepositoryCompiler, final FunctionCompilationContext functionCompilationContext) {
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
    return new ThreadPoolExecutor(1, Math.max(Runtime.getRuntime().availableProcessors() - 1, 1), 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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

  protected void initializeImpl(final long initId) {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing function definitions");
    // TODO kirk 2010-03-07 -- Better error handling.
    final ExecutorCompletionService<FunctionDefinition> completionService = new ExecutorCompletionService<FunctionDefinition>(getExecutorService());
    int nFunctions = getFunctionRepository().getAllFunctions().size();
    for (final FunctionDefinition definition : getFunctionRepository().getAllFunctions()) {
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
        throw new OpenGammaRuntimeException("Couldn't initialise function", e);
      }
    }
    getFunctionCompilationContext().setFunctionInitId(initId);
    timer.finished();
  }

  public void initialize() {
    // If the view processor node has restarted, remote nodes might have old values knocking around. We need a value
    // that won't "accidentally" be the same as theirs. As we increment the ID by 1 each time, the clock is possibly
    // a good choice unless we're clocking config changes at sub-millisecond speeds.
    initialize(System.currentTimeMillis());
  }

  public synchronized void initialize(final long initId) {
    s_logger.info("Initializing all function definitions to {}", initId);
    initializeImpl(initId);
  }

  public synchronized void reinitializeIfNeeded(final long initId) {
    if (getFunctionCompilationContext().getFunctionInitId() != initId) {
      s_logger.info("Re-initializing function definitions - was {} required {}", getFunctionCompilationContext().getFunctionInitId(), initId);
      initializeImpl(initId);
    }
  }

  public synchronized void reinitialize() {
    long initId = getFunctionCompilationContext().getFunctionInitId() + 1;
    s_logger.info("Re-initializing all function definitions to {}", initId);
    initializeImpl(initId);
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

}
