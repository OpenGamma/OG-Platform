/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.cache.msg.CacheMessage;
import com.opengamma.engine.cache.msg.DeleteRequest;
import com.opengamma.engine.cache.msg.GetRequest;
import com.opengamma.engine.cache.msg.GetResponse;
import com.opengamma.engine.cache.msg.PutRequest;

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
    final DeleteRequest request = new DeleteRequest(getCacheKey().getViewCycleId(), getCacheKey().getCalculationConfigurationName());
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

  @Override
  public FudgeMsg get(long identifier) {
    final GetRequest request = new GetRequest(getCacheKey().getViewCycleId(), getCacheKey()
        .getCalculationConfigurationName(), Collections.singleton(identifier));
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final FudgeMsg data = response.getData().get(0);
    return data.isEmpty() ? null : data;
  }

  @Override
  public Map<Long, FudgeMsg> get(Collection<Long> identifiers) {
    final GetRequest request = new GetRequest(getCacheKey().getViewCycleId(), getCacheKey()
        .getCalculationConfigurationName(), identifiers);
    final GetResponse response = getRemoteCacheClient().sendGetMessage(request, GetResponse.class);
    final Map<Long, FudgeMsg> result = new HashMap<Long, FudgeMsg>();
    final List<FudgeMsg> values = response.getData();
    if (values.size() != identifiers.size()) {
      // An error at the server end, possibly an invalid cache (gives a result with just one null in)
      return Collections.emptyMap();
    }
    int i = 0;
    for (Long identifier : request.getIdentifier()) {
      final FudgeMsg value = values.get(i++);
      if (!value.isEmpty()) {
        result.put(identifier, value);
      }
    }
    return result;
  }

  @Override
  public void put(long identifier, FudgeMsg data) {
    final PutRequest request = new PutRequest(getCacheKey().getViewCycleId(), getCacheKey()
        .getCalculationConfigurationName(), Collections.singleton(identifier),
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
    final PutRequest request = new PutRequest(getCacheKey().getViewCycleId(), getCacheKey()
        .getCalculationConfigurationName(), identifiers, values);
    getRemoteCacheClient().sendPutMessage(request, CacheMessage.class);
  }

}
