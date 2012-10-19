/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolverWithOverride;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Encapsulates the context required by a view process.
 */
public class ViewProcessContext {

  private final ViewDefinitionRepository _viewDefinitionRepository;
  private final ViewPermissionProvider _viewPermissionProvider;
  private final CompiledFunctionService _functionCompilationService;
  private final FunctionResolver _functionResolver;
  private final ViewComputationCacheSource _computationCacheSource;
  private final JobDispatcher _computationJobDispatcher;
  private final ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private final DependencyGraphBuilderFactory _dependencyGraphBuilderFactory;
  private final DependencyGraphExecutorFactory<?> _dependencyGraphExecutorFactory;
  private final GraphExecutorStatisticsGathererProvider _graphExecutorStatisticsGathererProvider;
  private final MarketDataInjector _liveDataOverrideInjector;
  private final MarketDataProviderResolver _marketDataProviderResolver;
  private final OverrideOperationCompiler _overrideOperationCompiler;

  public ViewProcessContext(
      ViewDefinitionRepository viewDefinitionRepository,
      ViewPermissionProvider viewPermissionProvider,
      MarketDataProviderResolver marketDataProviderResolver,
      CompiledFunctionService functionCompilationService,
      FunctionResolver functionResolver,
      ViewComputationCacheSource computationCacheSource,
      JobDispatcher computationJobDispatcher,
      ViewProcessorQueryReceiver viewProcessorQueryReceiver,
      DependencyGraphBuilderFactory dependencyGraphBuilderFactory,
      DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory,
      GraphExecutorStatisticsGathererProvider graphExecutorStatisticsProvider,
      OverrideOperationCompiler overrideOperationCompiler) {
    ArgumentChecker.notNull(viewDefinitionRepository, "viewDefinitionRepository");
    ArgumentChecker.notNull(viewPermissionProvider, "viewPermissionProvider");
    ArgumentChecker.notNull(marketDataProviderResolver, "marketDataSnapshotProviderResolver");
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(functionResolver, "functionResolver");
    ArgumentChecker.notNull(computationCacheSource, "computationCacheSource");
    ArgumentChecker.notNull(computationJobDispatcher, "computationJobDispatcher");
    ArgumentChecker.notNull(viewProcessorQueryReceiver, "viewProcessorQueryReceiver");
    ArgumentChecker.notNull(dependencyGraphBuilderFactory, "dependencyGraphBuilderFactory");
    ArgumentChecker.notNull(dependencyGraphExecutorFactory, "dependencyGraphExecutorFactory");
    ArgumentChecker.notNull(graphExecutorStatisticsProvider, "graphExecutorStatisticsProvider");
    ArgumentChecker.notNull(overrideOperationCompiler, "overrideOperationCompiler");
    _viewDefinitionRepository = viewDefinitionRepository;
    _viewPermissionProvider = viewPermissionProvider;
    final InMemoryLKVMarketDataProvider liveDataOverrideInjector = new InMemoryLKVMarketDataProvider(functionCompilationService.getFunctionCompilationContext().getSecuritySource());
    _liveDataOverrideInjector = liveDataOverrideInjector;
    _marketDataProviderResolver = new MarketDataProviderResolverWithOverride(marketDataProviderResolver, liveDataOverrideInjector);
    _functionCompilationService = functionCompilationService;
    _functionResolver = functionResolver;
    _computationCacheSource = computationCacheSource;
    _computationJobDispatcher = computationJobDispatcher;
    _viewProcessorQueryReceiver = viewProcessorQueryReceiver;
    _dependencyGraphBuilderFactory = dependencyGraphBuilderFactory;
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
    _graphExecutorStatisticsGathererProvider = graphExecutorStatisticsProvider;
    _overrideOperationCompiler = overrideOperationCompiler;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the view definition repository
   * 
   * @return the view definition repository, not null
   */
  public ViewDefinitionRepository getViewDefinitionRepository() {
    return _viewDefinitionRepository;
  }
  
  /**
   * Gets the view permission provider
   * 
   * @return the view permission provider, not null
   */
  public ViewPermissionProvider getViewPermissionProvider() {
    return _viewPermissionProvider;
  }
  
  /**
   * Gets the market data provider resolver.
   * 
   * @return the market data provider resolver, not null
   */
  public MarketDataProviderResolver getMarketDataProviderResolver() {
    return _marketDataProviderResolver;
  }

  /**
   * Gets the live data override injector.
   * 
   * @return the live data override injector, not null
   */
  public MarketDataInjector getLiveDataOverrideInjector() {
    return _liveDataOverrideInjector;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
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
   * Gets the dependency graph builder factory.
   * 
   * @return the dependency graph builder, not null
   */
  public DependencyGraphBuilderFactory getDependencyGraphBuilderFactory() {
    return _dependencyGraphBuilderFactory;
  }

  /**
   * Gets the computation cache source.
   * 
   * @return the computation cache source, not null
   */
  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  /**
   * Gets the computation job dispatcher.
   * 
   * @return the computation job dispatcher, not null
   */
  public JobDispatcher getComputationJobDispatcher() {
    return _computationJobDispatcher;
  }

  /**
   * Gets the view processor query receiver.
   * 
   * @return the view processor query receiver, not null
   */
  public ViewProcessorQueryReceiver getViewProcessorQueryReceiver() {
    return _viewProcessorQueryReceiver;
  }

  /**
   * Gets the dependency graph executor factory.
   * 
   * @return  the dependency graph executor factory, not null
   */
  public DependencyGraphExecutorFactory<?> getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public GraphExecutorStatisticsGathererProvider getGraphExecutorStatisticsGathererProvider() {
    return _graphExecutorStatisticsGathererProvider;
  }

  public OverrideOperationCompiler getOverrideOperationCompiler() {
    return _overrideOperationCompiler;
  }

  // -------------------------------------------------------------------------
  /**
   * Uses this context to form a {@code ViewCompliationServices} instance.
   * 
   * @param marketDataAvailabilityProvider  the availability provider corresponding to the desired source of market data, not null
   * @return the services, not null
   */
  public ViewCompilationServices asCompilationServices(MarketDataAvailabilityProvider marketDataAvailabilityProvider) {
    return new ViewCompilationServices(marketDataAvailabilityProvider, getFunctionResolver(), getFunctionCompilationService().getFunctionCompilationContext(), getFunctionCompilationService()
        .getExecutorService(), getDependencyGraphBuilderFactory());
  }

}
