/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.concurrent.ExecutorService;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-05-22 -- I don't like this name but couldn't come up with a better
// one on the fly.

/**
 * All the injected services necessary for view compilation.
 */
public class ViewCompilationServices {
  private final MarketDataAvailabilityProvider _marketDataAvailabilityProvider;
  private final FunctionResolver _functionResolver;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final ExecutorService _executorService;
  private final FunctionCompilationContext _compilationContext;
  private final CachingComputationTargetResolver _computationTargetResolver;
  
  /**
   * Constructs an instance, without a position source or security source.
   * 
   * @param marketDataAvailabilityProvider  the market data availability provider
   * @param functionResolver  the function resolver
   * @param compilationContext  the function compilation context
   * @param computationTargetResolver  the computation target resolver
   * @param executorService  the executor service
   */
  public ViewCompilationServices(
      MarketDataAvailabilityProvider marketDataAvailabilityProvider,
      FunctionResolver functionResolver,
      FunctionCompilationContext compilationContext,
      CachingComputationTargetResolver computationTargetResolver,
      ExecutorService executorService) {
    this(marketDataAvailabilityProvider, functionResolver, compilationContext, computationTargetResolver, executorService, null, null);
  }
  
  /**
   * Constructs an instance, without a position source.
   * 
   * @param marketDataAvailabilityProvider  the market data availability provider
   * @param functionResolver  the function resolver
   * @param compilationContext  the function compilation context
   * @param computationTargetResolver  the computation target resolver
   * @param executorService  the executor service
   * @param securitySource  the security source
   */
  public ViewCompilationServices(
      MarketDataAvailabilityProvider marketDataAvailabilityProvider,
      FunctionResolver functionResolver,
      FunctionCompilationContext compilationContext,
      CachingComputationTargetResolver computationTargetResolver,
      ExecutorService executorService,
      SecuritySource securitySource) {
    this(marketDataAvailabilityProvider, functionResolver, compilationContext, computationTargetResolver, executorService, securitySource, null);
  }
  
  /**
   * Constructs an instance
   * 
   * @param marketDataAvailabilityProvider  the market data availability provider
   * @param functionResolver  the function resolver
   * @param compilationContext  the function compilation context
   * @param computationTargetResolver  the computation target resolver
   * @param executorService  the executor service
   * @param securitySource  the security source
   * @param positionSource  the position source
   */
  public ViewCompilationServices(
      MarketDataAvailabilityProvider marketDataAvailabilityProvider,
      FunctionResolver functionResolver,
      FunctionCompilationContext compilationContext,
      CachingComputationTargetResolver computationTargetResolver,
      ExecutorService executorService,
      SecuritySource securitySource,
      PositionSource positionSource) {
    ArgumentChecker.notNull(marketDataAvailabilityProvider, "marketDataAvailabilityProvider");
    ArgumentChecker.notNull(functionResolver, "functionResolver");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    ArgumentChecker.notNull(computationTargetResolver, "computationTargetResolver");
    ArgumentChecker.notNull(executorService, "executorService");
    
    _marketDataAvailabilityProvider = marketDataAvailabilityProvider;
    _functionResolver = functionResolver;
    _compilationContext = compilationContext;
    _executorService = executorService;
    _computationTargetResolver = computationTargetResolver;
    _securitySource = securitySource;
    _positionSource = positionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the market data availability provider.
   * 
   * @return the market data availability provider, not {@code null}
   */
  public MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return _marketDataAvailabilityProvider;
  }

  /**
   * Gets the function resolver.
   * @return the function resolver, not null
   */
  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  /**
   * Gets the source of positions.
   * @return the source of positions, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Gets the source of securities.
   * @return the source of securities, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the executor service.
   * @return the executor service, not null
   */
  public ExecutorService getExecutorService() {
    return _executorService;
  }

  /**
   * Gets the compilation context.
   * @return the compilation context, not null
   */
  public FunctionCompilationContext getFunctionCompilationContext() {
    return _compilationContext;
  }

  /**
   * Gets the computation target resolver.
   * @return the computation target resolver, not null
   */
  public CachingComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

}
