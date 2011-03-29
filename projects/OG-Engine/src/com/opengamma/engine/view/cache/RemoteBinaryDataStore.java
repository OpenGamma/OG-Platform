/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.view.cache.msg.CacheMessage;
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
    // [ENG-256] Don't need the delete messages if we propogate at the releaseCaches level
    final DeleteRequest request = new DeleteRequest(getCacheKey().getViewProcessId(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp());
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public byte[] get(long identifier) {
    final GetRequest request = new GetRequest(getCacheKey().getViewProcessId(),
        getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), Collections.singleton(identifier));
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final byte[] data = response.getData().get(0);
    if (data.length > 0) {
      return data;
    } else {
      return null;
    }
  }

  @Override
  public Map<Long, byte[]> get(Collection<Long> identifiers) {
    final GetRequest request = new GetRequest(getCacheKey().getViewProcessId(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifiers);
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final Map<Long, byte[]> result = new HashMap<Long, byte[]>();
    final List<byte[]> values = response.getData();
    int i = 0;
    for (Long identifier : request.getIdentifier()) {
      final byte[] data = values.get(i++);
      if (data.length > 0) {
        result.put(identifier, data);
      }
    }
    return result;
  }

  @Override
  public void put(long identifier, byte[] data) {
    final PutRequest request = new PutRequest(getCacheKey().getViewProcessId(),
        getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), Collections.singleton(identifier),
        Collections.singleton(data));
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public void put(Map<Long, byte[]> data) {
    final List<Long> identifiers = new ArrayList<Long>(data.size());
    final List<byte[]> values = new ArrayList<byte[]>(data.size());
    for (Map.Entry<Long, byte[]> entry : data.entrySet()) {
      identifiers.add(entry.getKey());
      values.add(entry.getValue());
    }
    final PutRequest request = new PutRequest(getCacheKey().getViewProcessId(),
        getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifiers, values);
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

}
