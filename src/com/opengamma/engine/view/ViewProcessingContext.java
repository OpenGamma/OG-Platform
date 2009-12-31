/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.transport.FudgeRequestSender;

/**
 * A collection for everything relating to processing a particular view.
 *
 * @author kirk
 */
public class ViewProcessingContext {
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private final LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private final FunctionRepository _analyticFunctionRepository;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;
  private final ViewComputationCacheSource _computationCacheSource;
  private final FudgeRequestSender _computationJobRequestSender;
  private final DefaultComputationTargetResolver _computationTargetResolver;

  public ViewProcessingContext(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      LiveDataSnapshotProvider liveDataSnapshotProvider,
      FunctionRepository analyticFunctionRepository,
      PositionMaster positionMaster,
      SecurityMaster securityMaster,
      ViewComputationCacheSource computationCacheSource,
      FudgeRequestSender computationJobRequestSender
      ) {
    // TODO kirk 2009-09-25 -- Check Inputs
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
    _analyticFunctionRepository = analyticFunctionRepository;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _computationCacheSource = computationCacheSource;
    _computationJobRequestSender = computationJobRequestSender;
    
    _computationTargetResolver = new DefaultComputationTargetResolver(securityMaster, positionMaster);
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @return the liveDataSnapshotProvider
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * @return the analyticFunctionRepository
   */
  public FunctionRepository getAnalyticFunctionRepository() {
    return _analyticFunctionRepository;
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
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @return the computationCacheSource
   */
  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  /**
   * @return the computationJobRequestSender
   */
  public FudgeRequestSender getComputationJobRequestSender() {
    return _computationJobRequestSender;
  }

  /**
   * @return the computationTargetResolver
   */
  public DefaultComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

}
