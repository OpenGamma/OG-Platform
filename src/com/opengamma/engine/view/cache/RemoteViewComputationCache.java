/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class RemoteViewComputationCache implements ViewComputationCache {
  private final RemoteCacheClient _remoteClient;
  private final ViewComputationCacheKey _cacheKey;
  
  public RemoteViewComputationCache(RemoteCacheClient remoteClient, ViewComputationCacheKey cacheKey) {
    ArgumentChecker.checkNotNull(remoteClient, "Remote cache client");
    ArgumentChecker.checkNotNull(cacheKey, "Computation cache key");
    _remoteClient = remoteClient;
    _cacheKey = cacheKey;
  }

  /**
   * @return the remoteClient
   */
  public RemoteCacheClient getRemoteClient() {
    return _remoteClient;
  }

  /**
   * @return the cacheKey
   */
  public ViewComputationCacheKey getCacheKey() {
    return _cacheKey;
  }

  @Override
  public ComputedValue getValue(ValueSpecification specification) {
    return getRemoteClient().getValue(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), specification);
  }

  @Override
  public void putValue(ComputedValue value) {
    getRemoteClient().putValue(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), value);
  }

}
