/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Arrays;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.livedata.CombiningLiveDataSnapshotProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * A collection for everything relating to processing a particular view. This separates {@link View} from
 * {@link ViewProcessor}, allowing other types of processor to create and own a view.
 */
public class ViewProcessingContext {

  private final LiveDataEntitlementChecker _liveDataEntitlementChecker;
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private final LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private final LiveDataInjector _liveDataOverrideInjector;
  private final CompiledFunctionService _functionCompilationService;
  private final FunctionResolver _functionResolver;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final ViewComputationCacheSource _computationCacheSource;
  private final JobDispatcher _computationJobDispatcher;
  private final ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private final CachingComputationTargetResolver _computationTargetResolver;
  private final DependencyGraphExecutorFactory<?> _dependencyGraphExecutorFactory;
  private final ViewPermissionProvider _permissionProvider;
  private final GraphExecutorStatisticsGathererProvider _graphExecutorStatisticsGathererProvider;

  public ViewProcessingContext(LiveDataEntitlementChecker liveDataEntitlementChecker, LiveDataAvailabilityProvider liveDataAvailabilityProvider, LiveDataSnapshotProvider liveDataSnapshotProvider,
      CompiledFunctionService functionCompilationService, FunctionResolver functionResolver, PositionSource positionSource, SecuritySource securitySource,
      CachingComputationTargetResolver computationTargetResolver, ViewComputationCacheSource computationCacheSource, JobDispatcher computationJobDispatcher,
      ViewProcessorQueryReceiver viewProcessorQueryReceiver, DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory, ViewPermissionProvider permissionProvider,
      GraphExecutorStatisticsGathererProvider graphExecutorStatisticsProvider) {
    ArgumentChecker.notNull(liveDataEntitlementChecker, "liveDataEntitlementChecker");
    ArgumentChecker.notNull(liveDataAvailabilityProvider, "liveDataAvailabilityProvider");
    ArgumentChecker.notNull(liveDataSnapshotProvider, "liveDataSnapshotProvider");
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(functionResolver, "functionResolver");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(computationCacheSource, "computationCacheSource");
    ArgumentChecker.notNull(computationJobDispatcher, "computationJobDispatcher");
    ArgumentChecker.notNull(viewProcessorQueryReceiver, "viewProcessorQueryReceiver");
    ArgumentChecker.notNull(dependencyGraphExecutorFactory, "dependencyGraphExecutorFactory");
    ArgumentChecker.notNull(permissionProvider, "permissionProvider");
    ArgumentChecker.notNull(graphExecutorStatisticsProvider, "graphExecutorStatisticsProvider");

    _liveDataEntitlementChecker = liveDataEntitlementChecker;
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
    InMemoryLKVSnapshotProvider liveDataOverrideSnapshotProvider = new InMemoryLKVSnapshotProvider();
    _liveDataOverrideInjector = liveDataOverrideSnapshotProvider;
    _liveDataSnapshotProvider = new CombiningLiveDataSnapshotProvider(Arrays.asList(liveDataOverrideSnapshotProvider, liveDataSnapshotProvider));
    _functionCompilationService = functionCompilationService;
    _functionResolver = functionResolver;
    _positionSource = positionSource;
    _securitySource = securitySource;
    _computationTargetResolver = computationTargetResolver;
    _computationCacheSource = computationCacheSource;
    _computationJobDispatcher = computationJobDispatcher;
    _viewProcessorQueryReceiver = viewProcessorQueryReceiver;
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
    _permissionProvider = permissionProvider;
    _graphExecutorStatisticsGathererProvider = graphExecutorStatisticsProvider;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the live data entitlement checker.
   * 
   * @return the live data entitlement checker, not null
   */
  public LiveDataEntitlementChecker getLiveDataEntitlementChecker() {
    return _liveDataEntitlementChecker;
  }

  /**
   * Gets the live data.
   * 
   * @return the live data availability provider, not null
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * Gets the live data snapshot provider.
   * 
   * @return the live data snapshot provider, not null
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * Gets the live data override injector.
   * 
   * @return the live data override injector, not null
   */
  public LiveDataInjector getLiveDataOverrideInjector() {
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
   * Gets the source of positions.
   * 
   * @return the source of positions, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Gets the source of securities.
   * 
   * @return the source of securities, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
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
   * Gets the computation target resvoler. The target resolver is capable of returning fully
   * constructed portfolio graphs with all security and internal references resolved.
   * 
   * @return the computationTargetResolver, not null
   */
  public CachingComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  /**
   * Gets the dependency graph executor factory.
   * 
   * @return  the dependency graph executor factory, not null
   */
  public DependencyGraphExecutorFactory<?> getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  /**
   * Gets the view permission provider.
   * 
   * @return  the permission provider, not null
   */
  public ViewPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  public GraphExecutorStatisticsGathererProvider getGraphExecutorStatisticsGathererProvider() {
    return _graphExecutorStatisticsGathererProvider;
  }

  // -------------------------------------------------------------------------
  /**
   * Converts this context to a {@code ViewCompliationServices}.
   * 
   * @return the services, not null
   */
  public ViewCompilationServices asCompilationServices() {
    return new ViewCompilationServices(getLiveDataAvailabilityProvider(), getFunctionResolver(), getFunctionCompilationService().getFunctionCompilationContext(), getComputationTargetResolver(),
        getFunctionCompilationService().getExecutorService(), getSecuritySource(), getPositionSource());
  }

}
