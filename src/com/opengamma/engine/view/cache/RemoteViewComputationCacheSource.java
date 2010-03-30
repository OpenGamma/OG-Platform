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
  private final RemoteComputationCacheClient _remoteClient;
  
  public RemoteViewComputationCacheSource(RemoteComputationCacheClient remoteClient) {
    ArgumentChecker.checkNotNull(remoteClient, "Remote computation cache client");
    _remoteClient = remoteClient;
  }

  /**
   * @return the remoteClient
   */
  public RemoteComputationCacheClient getRemoteClient() {
    return _remoteClient;
  }

  @Override
  public ViewComputationCache cloneCache(String viewName,
      String calculationConfigurationName, long timestamp) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ViewComputationCache getCache(String viewName,
      String calculationConfigurationName, long timestamp) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void releaseCaches(String viewName, long timestamp) {
    // TODO Auto-generated method stub

  }
}
