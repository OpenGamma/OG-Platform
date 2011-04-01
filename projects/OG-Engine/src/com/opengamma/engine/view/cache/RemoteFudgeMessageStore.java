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

import org.fudgemsg.FudgeMsg;

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
    final DeleteRequest request = new DeleteRequest(getCacheKey().getViewName(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp());
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public FudgeMsg get(long identifier) {
    final GetRequest request = new GetRequest(getCacheKey().getViewName(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), Collections.singleton(identifier));
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final FudgeMsg data = response.getData().get(0);
    return data.isEmpty() ? null : data;
  }

  @Override
  public Map<Long, FudgeMsg> get(Collection<Long> identifiers) {
    final GetRequest request = new GetRequest(getCacheKey().getViewName(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifiers);
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final Map<Long, FudgeMsg> result = new HashMap<Long, FudgeMsg>();
    final List<FudgeMsg> values = response.getData();
    int i = 0;
    for (Long identifier : request.getIdentifier()) {
      result.put(identifier, values.get(i++));
    }
    return result;
  }

  @Override
  public void put(long identifier, FudgeMsg data) {
    final PutRequest request = new PutRequest(getCacheKey().getViewName(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), Collections.singleton(identifier),
        Collections.singleton(data));
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public void put(Map<Long, FudgeMsg> data) {
    final List<Long> identifiers = new ArrayList<Long>(data.size());
    final List<FudgeMsg> values = new ArrayList<FudgeMsg>(data.size());
    for (Map.Entry<Long, FudgeMsg> entry : data.entrySet()) {
      identifiers.add(entry.getKey());
      values.add(entry.getValue());
    }
    final PutRequest request = new PutRequest(getCacheKey().getViewName(), getCacheKey()
        .getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), identifiers, values);
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

}
