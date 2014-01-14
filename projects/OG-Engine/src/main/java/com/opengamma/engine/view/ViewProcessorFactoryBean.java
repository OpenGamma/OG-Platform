/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.cache.ViewComputationCacheSource;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.exec.DependencyGraphExecutorFactory;
import com.opengamma.engine.exec.SingleNodeExecutorFactory;
import com.opengamma.engine.exec.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.DummyOverrideOperationCompiler;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.engine.view.listener.ViewResultListenerFactory;
import com.opengamma.engine.view.permission.DefaultViewPortfolioPermissionProvider;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.engine.view.permission.ViewPortfolioPermissionProvider;
import com.opengamma.engine.view.worker.SingleThreadViewProcessWorkerFactory;
import com.opengamma.engine.view.worker.ViewProcessWorkerFactory;
import com.opengamma.engine.view.worker.cache.InMemoryViewExecutionCache;
import com.opengamma.engine.view.worker.cache.ViewExecutionCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link ViewProcessor}.
 */
public class ViewProcessorFactoryBean extends SingletonFactoryBean<ViewProcessor> {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorFactoryBean.class);

  private String _name;
  private ConfigSource _configSource;
  private NamedMarketDataSpecificationRepository _namedMarketDataSpecificationRepository;
  private CompiledFunctionService _functionCompilationService;
  private FunctionResolver _functionResolver;
  private MarketDataProviderResolver _marketDataProviderResolver;
  private ViewComputationCacheSource _computationCacheSource;
  private JobDispatcher _computationJobDispatcher;
  private ViewProcessWorkerFactory _viewProcessWorkerFactory = new SingleThreadViewProcessWorkerFactory();
  private DependencyGraphBuilderFactory _dependencyGraphBuilderFactory = new DependencyGraphBuilderFactory();
  private DependencyGraphExecutorFactory _dependencyGraphExecutorFactory = new SingleNodeExecutorFactory();
  private GraphExecutorStatisticsGathererProvider _graphExecutionStatistics = new DiscardingGraphStatisticsGathererProvider();
  private ViewPermissionProvider _viewPermissionProvider;
  private ViewPortfolioPermissionProvider _viewClientPortfolioPermissionProvider =
      new DefaultViewPortfolioPermissionProvider();
  private OverrideOperationCompiler _overrideOperationCompiler = new DummyOverrideOperationCompiler();
  private ViewResultListenerFactory _batchViewClientFactory;
  private ViewExecutionCache _viewExecutionCache = new InMemoryViewExecutionCache();
  private int _permissionCheckInterval;
  private boolean _useAutoStartViews;

  //-------------------------------------------------------------------------
  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  public NamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
    return _namedMarketDataSpecificationRepository;
  }

  public void setNamedMarketDataSpecificationRepository(final NamedMarketDataSpecificationRepository namedMarketDataSpecificationRepository) {
    _namedMarketDataSpecificationRepository = namedMarketDataSpecificationRepository;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public void setConfigSource(final ConfigSource configSource) {
    this._configSource = configSource;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public void setFunctionCompilationService(final CompiledFunctionService functionCompilationService) {
    _functionCompilationService = functionCompilationService;
  }

  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  public void setFunctionResolver(final FunctionResolver functionResolver) {
    _functionResolver = functionResolver;
  }

  public DependencyGraphBuilderFactory getDependencyGraphBuilderFactory() {
    return _dependencyGraphBuilderFactory;
  }

  public void setDependencyGraphBuilderFactory(final DependencyGraphBuilderFactory dependencyGraphBuilderFactory) {
    _dependencyGraphBuilderFactory = dependencyGraphBuilderFactory;
  }

  public MarketDataProviderResolver getMarketDataProviderResolver() {
    return _marketDataProviderResolver;
  }

  public void setMarketDataProviderResolver(final MarketDataProviderResolver marketDataProviderResolver) {
    _marketDataProviderResolver = marketDataProviderResolver;
  }

  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  public void setComputationCacheSource(final ViewComputationCacheSource computationCacheSource) {
    _computationCacheSource = computationCacheSource;
  }

  public JobDispatcher getComputationJobDispatcher() {
    return _computationJobDispatcher;
  }

  public void setComputationJobDispatcher(final JobDispatcher computationJobDispatcher) {
    _computationJobDispatcher = computationJobDispatcher;
  }

  public ViewProcessWorkerFactory getViewProcessWorkerFactory() {
    return _viewProcessWorkerFactory;
  }

  public void setViewProcessWorkerFactory(final ViewProcessWorkerFactory viewProcessWorkerFactory) {
    _viewProcessWorkerFactory = viewProcessWorkerFactory;
  }

  public DependencyGraphExecutorFactory getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(final DependencyGraphExecutorFactory dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }

  public GraphExecutorStatisticsGathererProvider getGraphExecutionStatistics() {
    return _graphExecutionStatistics;
  }

  public void setGraphExecutionStatistics(final GraphExecutorStatisticsGathererProvider graphExecutionStatistics) {
    _graphExecutionStatistics = graphExecutionStatistics;
  }

  public ViewPermissionProvider getViewPermissionProvider() {
    return _viewPermissionProvider;
  }

  public void setViewPermissionProvider(final ViewPermissionProvider viewPermissionProvider) {
    _viewPermissionProvider = viewPermissionProvider;
  }

  public OverrideOperationCompiler getOverrideOperationCompiler() {
    return _overrideOperationCompiler;
  }

  public void setOverrideOperationCompiler(final OverrideOperationCompiler overrideOperationCompiler) {
    _overrideOperationCompiler = overrideOperationCompiler;
  }

  public ViewExecutionCache getViewExecutionCache() {
    return _viewExecutionCache;
  }

  public void setViewExecutionCache(final ViewExecutionCache viewExecutionCache) {
    _viewExecutionCache = viewExecutionCache;
  }

  public void setUseAutoStartViews(boolean useAutoStartViews) {
    _useAutoStartViews = useAutoStartViews;
  }

  public void setPermissionCheckInterval(int permissionCheckInterval) {
    _permissionCheckInterval = permissionCheckInterval;
  }

  //-------------------------------------------------------------------------
  protected void checkInjectedInputs() {
    s_logger.debug("Checking injected inputs.");
    ArgumentChecker.notNullInjected(_name, "id");
    ArgumentChecker.notNullInjected(getFunctionCompilationService(), "functionCompilationService");
    if (getFunctionResolver() == null) {
      setFunctionResolver(new DefaultFunctionResolver(getFunctionCompilationService()));
    }
    ArgumentChecker.notNullInjected(getMarketDataProviderResolver(), "marketDataProviderResolver");
    ArgumentChecker.notNullInjected(getComputationCacheSource(), "computationCacheSource");
    ArgumentChecker.notNullInjected(getComputationJobDispatcher(), "computationJobDispatcher");
    ArgumentChecker.notNullInjected(getViewProcessWorkerFactory(), "viewComputationJobFactory");
    ArgumentChecker.notNullInjected(getViewPermissionProvider(), "viewPermissionProvider");
    ArgumentChecker.notNullInjected(getViewExecutionCache(), "viewExecutionCache");
  }

  @Override
  public ViewProcessor createObject() {
    checkInjectedInputs();
    return new ViewProcessorImpl(
        getName(),
        getConfigSource(),
        getNamedMarketDataSpecificationRepository(),
        getFunctionCompilationService(),
        getFunctionResolver(),
        getMarketDataProviderResolver(),
        getComputationCacheSource(),
        getComputationJobDispatcher(),
        getDependencyGraphBuilderFactory(),
        getDependencyGraphExecutorFactory(),
        getGraphExecutionStatistics(),
        getViewPermissionProvider(),
        getViewPortfolioPermissionProvider(),
        getOverrideOperationCompiler(),
        getViewResultListenerFactory(),
        getViewProcessWorkerFactory(),
        getViewExecutionCache(),
        _permissionCheckInterval,
        _useAutoStartViews);
  }

  public void setViewResultListenerFactory(final ViewResultListenerFactory viewResultListenerFactory) {
    _batchViewClientFactory = viewResultListenerFactory;
  }

  public ViewResultListenerFactory getViewResultListenerFactory() {
    return _batchViewClientFactory;
  }

  public ViewPortfolioPermissionProvider getViewPortfolioPermissionProvider() {
    return _viewClientPortfolioPermissionProvider;
  }

  public void setViewPortfolioPermissionProvider(ViewPortfolioPermissionProvider permissionProvider) {
    _viewClientPortfolioPermissionProvider = permissionProvider;
  }
}
