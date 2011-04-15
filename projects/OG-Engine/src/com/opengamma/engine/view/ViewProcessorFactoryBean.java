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
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermissionProviderFactory;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataClient;
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
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private CachingComputationTargetResolver _computationTargetResolver;
  private CompiledFunctionService _functionCompilationService;
  private FunctionResolver _functionResolver;
  private LiveDataClient _liveDataClient;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private ViewComputationCacheSource _computationCacheSource;
  private JobDispatcher _computationJobDispatcher;
  private ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private DependencyGraphExecutorFactory<?> _dependencyGraphExecutorFactory;
  private GraphExecutorStatisticsGathererProvider _graphExecutionStatistics = new DiscardingGraphStatisticsGathererProvider();
  private ViewPermissionProviderFactory _permissionProviderFactory;
  
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

  public LiveDataClient getLiveDataClient() {
    return _liveDataClient;
  }

  public void setLiveDataClient(LiveDataClient liveDataClient) {
    _liveDataClient = liveDataClient;
  }

  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  public void setLiveDataAvailabilityProvider(LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  public void setLiveDataSnapshotProvider(LiveDataSnapshotProvider liveDataSnapshotProvider) {
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
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
  
  public ViewPermissionProviderFactory getViewPermissionProviderFactory() {
    return _permissionProviderFactory;
  }
  
  public void setViewPermissionProviderFactory(ViewPermissionProviderFactory permissionProviderFactory) {
    _permissionProviderFactory = permissionProviderFactory;
  }
  
  //-------------------------------------------------------------------------
  protected void checkInjectedInputs() {
    s_logger.debug("Checking injected inputs.");
    ArgumentChecker.notNullInjected(_id, "id");
    ArgumentChecker.notNullInjected(getViewDefinitionRepository(), "viewDefinitionRepository");
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
    ArgumentChecker.notNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getLiveDataSnapshotProvider(), "liveDataSnapshotProvider");
    ArgumentChecker.notNullInjected(getComputationCacheSource(), "computationCacheSource");
    ArgumentChecker.notNullInjected(getComputationJobDispatcher(), "computationJobRequestSender");
    ArgumentChecker.notNullInjected(getViewPermissionProviderFactory(), "viewPermissionProviderFactory");
  }

  @Override
  public ViewProcessor createObject() {
    checkInjectedInputs();
    return new ViewProcessorImpl(
        UniqueIdentifier.of(VIEW_PROCESSOR_ID_SCHEME, getId().toString()),
        getViewDefinitionRepository(),
        getSecuritySource(),
        getPositionSource(),
        getComputationTargetResolver(),
        getFunctionCompilationService(),
        getFunctionResolver(),
        getLiveDataClient(),
        getLiveDataAvailabilityProvider(),
        getLiveDataSnapshotProvider(),
        getComputationCacheSource(),
        getComputationJobDispatcher(),
        getViewProcessorQueryReceiver(),
        getDependencyGraphExecutorFactory(),
        getGraphExecutionStatistics(),
        getViewPermissionProviderFactory());
  }

}
