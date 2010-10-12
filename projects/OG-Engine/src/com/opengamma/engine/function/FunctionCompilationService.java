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
 * Combines a function repository and repository to give access to compiled functions. 
 */
// ENG-247 rename this to CompiledFunctionService as it is not the compiler but provider of compiled function definitions
public class FunctionCompilationService {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionCompilationService.class);

  private final FunctionRepository _functionRepository;
  private final FunctionRepositoryCompiler _functionRepositoryCompiler;
  private final FunctionCompilationContext _functionCompilationContext;
  private ExecutorService _executorService;
  private boolean _initialized;

  public FunctionCompilationService(final FunctionRepository functionRepository, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext) {
    ArgumentChecker.notNull(functionRepository, "functionRepository");
    ArgumentChecker.notNull(functionRepositoryCompiler, "functionRepositoryCompiler");
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    _functionRepository = functionRepository;
    _functionRepositoryCompiler = functionRepositoryCompiler;
    _functionCompilationContext = functionCompilationContext;
  }

  public void setExecutorService(final ExecutorService executorService) {
    ArgumentChecker.notNull(executorService, "executorService");
    _executorService = executorService;
  }

  protected void initializeImpl() {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing function definitions");
    s_logger.info("Initializing all function definitions.");
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
    timer.finished();
  }

  public synchronized void initialize() {
    if (!_initialized) {
      if (getExecutorService() == null) {
        setExecutorService(new ThreadPoolExecutor(1, Math.max(Runtime.getRuntime().availableProcessors() - 1, 1), 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
      }
      initializeImpl();
      _initialized = true;
    } else {
      s_logger.debug("Function definitions already initialized");
    }
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
    return getFunctionRepositoryCompiler().compile(this, Instant.ofEpochMillis(timestamp));
  }

  public CompiledFunctionRepository compileFunctionRepository(final InstantProvider timestamp) {
    return getFunctionRepositoryCompiler().compile(this, timestamp);
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

}
