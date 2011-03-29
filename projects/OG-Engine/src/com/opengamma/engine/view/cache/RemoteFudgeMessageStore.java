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

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.DeleteRequest;
import com.opengamma.engine.view.cache.msg.GetRequest;
import com.opengamma.engine.view.cache.msg.GetResponse;
import com.opengamma.engine.view.cache.msg.PutRequest;

/**
 * Client to a {@link FudgeMessageStoreServer}. These are created by a {@link RemoteFudgeMessageStoreFactory}.
 */
public class RemoteFudgeMessageStore implements FudgeMessageStore {

  private final RemoteCacheClient _client;
  private final ViewComputationCacheKey _cacheKey;

  public RemoteFudgeMessageStore(final RemoteCacheClient client, final ViewComputationCacheKey cacheKey) {
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
    final DeleteRequest request = new DeleteRequest(getCacheKey().getViewProcessId(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp());
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public FudgeFieldContainer get(long identifier) {
    final GetRequest request = new GetRequest(getCacheKey().getViewProcessId(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), Collections.singleton(identifier));
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final FudgeFieldContainer data = response.getData().get(0);
    return data.isEmpty() ? null : data;
  }

  @Override
  public Map<Long, FudgeFieldContainer> get(Collection<Long> identifiers) {
    final GetRequest request = new GetRequest(getCacheKey().getViewProcessId(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifiers);
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final Map<Long, FudgeFieldContainer> result = new HashMap<Long, FudgeFieldContainer>();
    final List<FudgeFieldContainer> values = response.getData();
    int i = 0;
    for (Long identifier : request.getIdentifier()) {
      result.put(identifier, values.get(i++));
    }
    return result;
  }

  @Override
  public void put(long identifier, FudgeFieldContainer data) {
    final PutRequest request = new PutRequest(getCacheKey().getViewProcessId(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), Collections.singleton(identifier),
        Collections.singleton(data));
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public void put(Map<Long, FudgeFieldContainer> data) {
    final List<Long> identifiers = new ArrayList<Long>(data.size());
    final List<FudgeFieldContainer> values = new ArrayList<FudgeFieldContainer>(data.size());
    for (Map.Entry<Long, FudgeFieldContainer> entry : data.entrySet()) {
      identifiers.add(entry.getKey());
      values.add(entry.getValue());
    }
    final PutRequest request = new PutRequest(getCacheKey().getViewProcessId(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifiers, values);
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

}
