/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;


/**
 * Creates {@link RemoteBinaryDataStore} clients to connect to a {@link BinaryDataStoreServer}.
 */
public class RemoteBinaryDataStoreFactory implements BinaryDataStoreFactory {

  private final RemoteCacheClient _client;

  public RemoteBinaryDataStoreFactory(final RemoteCacheClient client) {
    _client = client;
  }

  protected RemoteCacheClient getRemoteCacheClient() {
    return _client;
  }

  @Override
  public BinaryDataStore createDataStore(final ViewComputationCacheKey cacheKey) {
    return new RemoteBinaryDataStore(getRemoteCacheClient(), cacheKey);
  }

}
