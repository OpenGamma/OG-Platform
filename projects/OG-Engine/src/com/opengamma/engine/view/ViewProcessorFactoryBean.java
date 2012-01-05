/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.DummyOverrideOperationCompiler;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Spring factory bean for {@link ViewProcessor}.
 */
public class ViewProcessorFactoryBean extends SingletonFactoryBean<ViewProcessor> {

  private static final String VIEW_PROCESSOR_ID_SCHEME = "Vp";
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorFactoryBean.class);
  
  private Long _id;
  private ViewDefinitionRepository _viewDefinitionRepository;
  private LiveMarketDataSourceRegistry _liveMarketDataSourceRegistry;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private CachingComputationTargetResolver _computationTargetResolver;
  private CompiledFunctionService _functionCompilationService;
  private FunctionResolver _functionResolver;
  private MarketDataProviderResolver _marketDataProviderResolver;
  private ViewComputationCacheSource _computationCacheSource;
  private JobDispatcher _computationJobDispatcher;
  private ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private DependencyGraphExecutorFactory<?> _dependencyGraphExecutorFactory;
  private GraphExecutorStatisticsGathererProvider _graphExecutionStatistics = new DiscardingGraphStatisticsGathererProvider();
  private ViewPermissionProvider _viewPermissionProvider;
  private OverrideOperationCompiler _overrideOperationCompiler = new DummyOverrideOperationCompiler();
  
  //-------------------------------------------------------------------------
  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  public ViewDefinitionRepository getViewDefinitionRepository() {
    return _viewDefinitionRepository;
  }

  public void setViewDefinitionRepository(ViewDefinitionRepository viewDefinitionRepository) {
    _viewDefinitionRepository = viewDefinitionRepository;
  }
  
  public LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry() {
    return _liveMarketDataSourceRegistry;
  }

  public void setLiveMarketDataSourceRegistry(LiveMarketDataSourceRegistry liveMarketDataSourceRegistry) {
    _liveMarketDataSourceRegistry = liveMarketDataSourceRegistry;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public void setPositionSource(PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public CachingComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  public void setComputationTargetResolver(CachingComputationTargetResolver computationTargetResolver) {
    _computationTargetResolver = computationTargetResolver;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public void setFunctionCompilationService(CompiledFunctionService functionCompilationService) {
    _functionCompilationService = functionCompilationService;
  }

  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  public void setFunctionResolver(FunctionResolver functionResolver) {
    _functionResolver = functionResolver;
  }

  public MarketDataProviderResolver getMarketDataProviderResolver() {
    return _marketDataProviderResolver;
  }

  public void setMarketDataProviderResolver(MarketDataProviderResolver marketDataProviderResolver) {
    _marketDataProviderResolver = marketDataProviderResolver;
  }

  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  public void setComputationCacheSource(ViewComputationCacheSource computationCacheSource) {
    _computationCacheSource = computationCacheSource;
  }

  public JobDispatcher getComputationJobDispatcher() {
    return _computationJobDispatcher;
  }

  public void setComputationJobDispatcher(JobDispatcher computationJobDispatcher) {
    _computationJobDispatcher = computationJobDispatcher;
  }

  public ViewProcessorQueryReceiver getViewProcessorQueryReceiver() {
    return _viewProcessorQueryReceiver;
  }

  public void setViewProcessorQueryReceiver(ViewProcessorQueryReceiver viewProcessorQueryReceiver) {
    _viewProcessorQueryReceiver = viewProcessorQueryReceiver;
  }

  public DependencyGraphExecutorFactory<?> getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }

  public GraphExecutorStatisticsGathererProvider getGraphExecutionStatistics() {
    return _graphExecutionStatistics;
  }

  public void setGraphExecutionStatistics(GraphExecutorStatisticsGathererProvider graphExecutionStatistics) {
    _graphExecutionStatistics = graphExecutionStatistics;
  }
  
  public ViewPermissionProvider getViewPermissionProvider() {
    return _viewPermissionProvider;
  }
  
  public void setViewPermissionProvider(ViewPermissionProvider viewPermissionProvider) {
    _viewPermissionProvider = viewPermissionProvider;
  }

  public OverrideOperationCompiler getOverrideOperationCompiler() {
    return _overrideOperationCompiler;
  }

  public void setOverrideOperationCompiler(final OverrideOperationCompiler overrideOperationCompiler) {
    _overrideOperationCompiler = overrideOperationCompiler;
  }

  //-------------------------------------------------------------------------
  protected void checkInjectedInputs() {
    s_logger.debug("Checking injected inputs.");
    ArgumentChecker.notNullInjected(_id, "id");
    ArgumentChecker.notNullInjected(getViewDefinitionRepository(), "viewDefinitionRepository");
    ArgumentChecker.notNullInjected(getLiveMarketDataSourceRegistry(), "liveMarketDataSourceRegistry");
    ArgumentChecker.notNullInjected(getFunctionCompilationService(), "functionCompilationService");
    if (getFunctionResolver() == null) {
      setFunctionResolver(new DefaultFunctionResolver(getFunctionCompilationService()));
    }
    ArgumentChecker.notNullInjected(getSecuritySource(), "securitySource");
    ArgumentChecker.notNullInjected(getPositionSource(), "positionSource");
    ArgumentChecker.notNullInjected(getComputationTargetResolver(), "computationTargetResolver");
    if (getComputationTargetResolver() == null) {
      setComputationTargetResolver(new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(getSecuritySource(), getPositionSource()), EHCacheUtils.createCacheManager()));
    }
    ArgumentChecker.notNullInjected(getMarketDataProviderResolver(), "marketDataProviderResolver");
    ArgumentChecker.notNullInjected(getComputationCacheSource(), "computationCacheSource");
    ArgumentChecker.notNullInjected(getComputationJobDispatcher(), "computationJobRequestSender");
    ArgumentChecker.notNullInjected(getViewPermissionProvider(), "viewPermissionProvider");
  }

  @Override
  public ViewProcessor createObject() {
    checkInjectedInputs();
    return new ViewProcessorImpl(
        UniqueId.of(VIEW_PROCESSOR_ID_SCHEME, getId().toString()),
        getViewDefinitionRepository(),
        getLiveMarketDataSourceRegistry(),
        getSecuritySource(),
        getPositionSource(),
        getComputationTargetResolver(),
        getFunctionCompilationService(),
        getFunctionResolver(),
        getMarketDataProviderResolver(),
        getComputationCacheSource(),
        getComputationJobDispatcher(),
        getViewProcessorQueryReceiver(),
        getDependencyGraphExecutorFactory(),
        getGraphExecutionStatistics(),
        getViewPermissionProvider(),
        getOverrideOperationCompiler());
  }

}
