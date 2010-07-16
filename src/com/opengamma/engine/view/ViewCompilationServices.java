/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.concurrent.ExecutorService;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-05-22 -- I don't like this name but couldn't come up with a better
// one on the fly.

/**
 * All the injected services necessary for view compilation.
 */
public class ViewCompilationServices {
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private final FunctionResolver _functionResolver;
  private final PositionMaster _positionMaster;
  private final SecuritySource _securityMaster;
  private final ExecutorService _executorService;
  private final FunctionCompilationContext _compilationContext;
  private final ComputationTargetResolver _computationTargetResolver;
  
  public ViewCompilationServices(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      FunctionResolver functionResolver,
      PositionMaster positionMaster,
      SecuritySource securityMaster,
      FunctionCompilationContext compilationContext,
      ComputationTargetResolver computationTargetResolver,
      ExecutorService executorService) {
    ArgumentChecker.notNull(liveDataAvailabilityProvider, "LiveDataAvailabilityProvider");
    ArgumentChecker.notNull(functionResolver, "FunctionResolver");
    ArgumentChecker.notNull(positionMaster, "PositionMaster");
    ArgumentChecker.notNull(securityMaster, "SecurityMaster");
    ArgumentChecker.notNull(compilationContext, "CompilationContext");
    ArgumentChecker.notNull(computationTargetResolver, "Computation target resolver");
    ArgumentChecker.notNull(executorService, "ExecutorService");
    
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
    _functionResolver = functionResolver;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _compilationContext = compilationContext;
    _executorService = executorService;
    _computationTargetResolver = computationTargetResolver;
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @return the functionResolver
   */
  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  /**
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * @return the securityMaster
   */
  public SecuritySource getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @return the executorService
   */
  public ExecutorService getExecutorService() {
    return _executorService;
  }

  /**
   * @return the compilationContext
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  /**
   * @return the computationTargetResolver
   */
  public ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }
  
}
