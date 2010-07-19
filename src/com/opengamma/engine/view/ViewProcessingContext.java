/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.concurrent.ExecutorService;

import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.FunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calcnode.JobRequestSender;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * A collection for everything relating to processing a particular view.
 */
public class ViewProcessingContext {

  private final LiveDataEntitlementChecker _liveDataEntitlementChecker;
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private final LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private final FunctionRepository _functionRepository;
  private final FunctionResolver _functionResolver;
  private final PositionMaster _positionMaster;
  private final SecuritySource _securitySource;
  private final ViewComputationCacheSource _computationCacheSource;
  private final JobRequestSender _computationJobRequestSender;
  private final ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private final ComputationTargetResolver _computationTargetResolver;
  private final FunctionCompilationContext _compilationContext;
  private final ExecutorService _executorService;

  public ViewProcessingContext(
      LiveDataEntitlementChecker liveDataEntitlementChecker,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      LiveDataSnapshotProvider liveDataSnapshotProvider,
      FunctionRepository functionRepository,
      FunctionResolver functionResolver,
      PositionMaster positionMaster,
      SecuritySource securitySource,
      ViewComputationCacheSource computationCacheSource,
      JobRequestSender computationJobRequestSender,
      ViewProcessorQueryReceiver viewProcessorQueryReceiver,
      FunctionCompilationContext compilationContext,
      ExecutorService executorService) {
    ArgumentChecker.notNull(liveDataEntitlementChecker, "LiveDataEntitlementChecker");
    ArgumentChecker.notNull(liveDataAvailabilityProvider, "LiveDataAvailabilityProvider");
    ArgumentChecker.notNull(liveDataSnapshotProvider, "LiveDataSnapshotProvier");
    ArgumentChecker.notNull(functionRepository, "FunctionRepository");
    ArgumentChecker.notNull(functionResolver, "FunctionResolver");
    ArgumentChecker.notNull(positionMaster, "PositionMaster");
    ArgumentChecker.notNull(securitySource, "SecuritySource");
    ArgumentChecker.notNull(computationCacheSource, "ComputationCacheSource");
    ArgumentChecker.notNull(computationJobRequestSender, "ComputationJobRequestSender");
    ArgumentChecker.notNull(viewProcessorQueryReceiver, "ViewProcessorQueryReceiver");
    ArgumentChecker.notNull(compilationContext, "CompilationContext");
    ArgumentChecker.notNull(executorService, "ExecutorService");
    
    _liveDataEntitlementChecker = liveDataEntitlementChecker;
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
    _functionRepository = functionRepository;
    _functionResolver = functionResolver;
    _positionMaster = positionMaster;
    _securitySource = securitySource;
    _computationCacheSource = computationCacheSource;
    _computationJobRequestSender = computationJobRequestSender;
    _viewProcessorQueryReceiver = viewProcessorQueryReceiver;
    _compilationContext = compilationContext;
    _executorService = executorService;
    
    // REVIEW kirk 2010-05-22 -- This isn't the right place to wrap this.
    _computationTargetResolver = new CachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionMaster));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the live data entitlement checker.
   * @return the live data entitlement checker, not null
   */
  public LiveDataEntitlementChecker getLiveDataEntitlementChecker() {
    return _liveDataEntitlementChecker;
  }

  /**
   * Gets the live data.
   * @return the live data availability provider, not null
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * Gets the live data snapshot provider.
   * @return the live data snapshot provider, not null
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * Gets the function repository.
   * @return the function repository, not null
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
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
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Gets the source of securities.
   * @return the source of securities, not null
   */
  public SecuritySource getSecurityMaster() {
    return _securitySource;
  }

  /**
   * Gets the computation cache source.
   * @return the computation cache source, not null
   */
  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  /**
   * Gets the computation job request sender.
   * @return the computation job request sender, not null
   */
  public JobRequestSender getComputationJobRequestSender() {
    return _computationJobRequestSender;
  }

  /**
   * Gets the view processor query receiver.
   * @return the view processor query receiver, not null
   */
  public ViewProcessorQueryReceiver getViewProcessorQueryReceiver() {
    return _viewProcessorQueryReceiver;
  }

  /**
   * Returns a {@code ComputationTargetResolver} constructed from the position and security master. The
   * target resolver is capable of returning fully constructed portfolio graphs with all security and
   * internal references resolved.
   * 
   * @return the computationTargetResolver, not null
   */
  public ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  /**
   * Gets the compilation context.
   * @return the compilation context, not null
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  /**
   * Gets the executor service.
   * @return the executor service, not null
   */
  public ExecutorService getExecutorService() {
    return _executorService;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this context to a {@code ViewCompliationServices}.
   * @return the services, not null
   */
  public ViewCompilationServices asCompilationServices() {
    return new ViewCompilationServices(
        getLiveDataAvailabilityProvider(),
        getFunctionResolver(),
        getPositionMaster(),
        getSecurityMaster(),
        getCompilationContext(),
        getComputationTargetResolver(),
        getExecutorService());
  }

}
