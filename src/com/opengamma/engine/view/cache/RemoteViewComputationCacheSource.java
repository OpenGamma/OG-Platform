/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class RemoteViewComputationCacheSource implements
    ViewComputationCacheSource {
  private final RemoteCacheClient _remoteClient;
  
  public RemoteViewComputationCacheSource(RemoteCacheClient remoteClient) {
    ArgumentChecker.checkNotNull(remoteClient, "Remote computation cache client");
    _remoteClient = remoteClient;
  }

  /**
   * @return the remoteClient
   */
  public RemoteCacheClient getRemoteClient() {
    return _remoteClient;
  }

  @Override
  public ViewComputationCache cloneCache(String viewName,
      String calculationConfigurationName, long timestamp) {
    throw new UnsupportedOperationException("Cloning not yet supported.");
  }

  @Override
  public ViewComputationCache getCache(String viewName,
      String calculationConfigurationName, long timestamp) {
    return new RemoteViewComputationCache(getRemoteClient(), new ViewComputationCacheKey(viewName, calculationConfigurationName, timestamp));
  }

  @Override
  public void releaseCaches(String viewName, long timestamp) {
    getRemoteClient().purgeCache(viewName, null, timestamp);
  }
}
