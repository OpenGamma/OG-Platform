/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.view.cache.msg.BinaryDataStoreResponse;
import com.opengamma.engine.view.cache.msg.DeleteRequest;
import com.opengamma.engine.view.cache.msg.GetRequest;
import com.opengamma.engine.view.cache.msg.GetResponse;
import com.opengamma.engine.view.cache.msg.PutRequest;

/**
 * Client to a {@link BinaryDataStoreServer}. These are created by a {@link RemoteBinaryDataStoreFactory}.
 */
public class RemoteBinaryDataStore implements BinaryDataStore {

  private final RemoteCacheClient _client;
  private final ViewComputationCacheKey _cacheKey;

  public RemoteBinaryDataStore(final RemoteCacheClient client, final ViewComputationCacheKey cacheKey) {
    _client = client;
    _cacheKey = cacheKey;
  }

  protected RemoteCacheClient getRemoteCacheClient() {
    return _client;
  }

  protected ViewComputationCacheKey getCacheKey() {
    return _cacheKey;
  }

  @Override
  public void delete() {
    final DeleteRequest request = new DeleteRequest(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp());
    getRemoteCacheClient().sendMessage(request, BinaryDataStoreResponse.class);
  }

  @Override
  public byte[] get(long identifier) {
    final GetRequest request = new GetRequest(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifier);
    final GetResponse response = getRemoteCacheClient().sendMessage(request, GetResponse.class);
    return response.getData();
  }

  @Override
  public void put(long identifier, byte[] data) {
    final PutRequest request = new PutRequest(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifier, data);
    getRemoteCacheClient().sendMessage(request, BinaryDataStoreResponse.class);
  }

}
