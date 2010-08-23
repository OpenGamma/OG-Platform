/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Initializes all function definitions. 
 */
public class FunctionCompilationService {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionCompilationService.class);

  private final FunctionRepository _functionRepository;
  private final FunctionCompilationContext _functionCompilationContext;
  private boolean _initialized;

  public FunctionCompilationService(final FunctionRepository functionRepository, final FunctionCompilationContext functionCompilationContext) {
    ArgumentChecker.notNull(functionRepository, "functionRepository");
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    _functionRepository = functionRepository;
    _functionCompilationContext = functionCompilationContext;
  }

  protected void initializeImpl(final ExecutorService executorService) {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing function definitions");
    s_logger.info("Initializing all function definitions.");
    // TODO kirk 2010-03-07 -- Better error handling.
    ExecutorCompletionService<FunctionDefinition> completionService = new ExecutorCompletionService<FunctionDefinition>(executorService);
    int nFunctions = getFunctionRepository().getAllFunctions().size();
    for (FunctionDefinition definition : getFunctionRepository().getAllFunctions()) {
      final FunctionDefinition finalDefinition = definition;
      completionService.submit(new Runnable() {
        @Override
        public void run() {
          try {
            finalDefinition.init(getFunctionCompilationContext());
          } catch (RuntimeException e) {
            s_logger.warn("Exception thrown while initializing FunctionDefinition {}-{}", new Object[] {finalDefinition, finalDefinition.getShortName()}, e);
            throw e;
          }
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
        // REVIEW kirk 2010-03-07 -- What do we do here?
      }
    }
    timer.finished();
  }

  public synchronized void initialize(final ExecutorService executorService) {
    if (!_initialized) {
      initializeImpl(executorService);
      _initialized = true;
    } else {
      s_logger.debug("Function definitions already initialized");
    }
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

}
