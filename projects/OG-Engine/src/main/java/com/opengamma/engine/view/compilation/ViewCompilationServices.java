/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;

// REVIEW kirk 2010-05-22 -- I don't like this name but couldn't come up with a better
// one on the fly.

/**
 * All the injected services necessary for view compilation.
 */
public class ViewCompilationServices {

  private final MarketDataAvailabilityProvider _marketDataAvailabilityProvider;
  private final FunctionResolver _functionResolver;
  private final PoolExecutor _executorService;
  private final FunctionCompilationContext _compilationContext;
  private final DependencyGraphBuilderFactory _dependencyGraphBuilder;

  /**
   * Constructs an instance
   * 
   * @param marketDataAvailabilityProvider the market data availability provider
   * @param functionResolver the function resolver
   * @param compilationContext the function compilation context
   * @param executorService the executor service
   * @param dependencyGraphBuilder the graph building implementation
   */
  public ViewCompilationServices(
      MarketDataAvailabilityProvider marketDataAvailabilityProvider,
      FunctionResolver functionResolver,
      FunctionCompilationContext compilationContext,
      PoolExecutor executorService,
      DependencyGraphBuilderFactory dependencyGraphBuilder) {
    ArgumentChecker.notNull(marketDataAvailabilityProvider, "marketDataAvailabilityProvider");
    ArgumentChecker.notNull(functionResolver, "functionResolver");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    ArgumentChecker.notNull(executorService, "executorService");
    ArgumentChecker.notNull(dependencyGraphBuilder, "dependencyGraphBuilder");
    _marketDataAvailabilityProvider = marketDataAvailabilityProvider;
    _functionResolver = functionResolver;
    _compilationContext = compilationContext;
    _executorService = executorService;
    _dependencyGraphBuilder = dependencyGraphBuilder;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the market data availability provider.
   * 
   * @return the market data availability provider, not null
   */
  public MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return _marketDataAvailabilityProvider;
  }

  /**
   * Gets the function resolver.
   * 
   * @return the function resolver, not null
   */
  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  /**
   * Gets the executor service.
   * 
   * @return the executor service, not null
   */
  public PoolExecutor getExecutorService() {
    return _executorService;
  }

  /**
   * Gets the compilation context.
   * 
   * @return the compilation context, not null
   */
  public FunctionCompilationContext getFunctionCompilationContext() {
    return _compilationContext;
  }

  /**
   * Gets the dependency graph builder factory.
   * 
   * @return the dependency graph builder factory, not null
   */
  public DependencyGraphBuilderFactory getDependencyGraphBuilder() {
    return _dependencyGraphBuilder;
  }

}
