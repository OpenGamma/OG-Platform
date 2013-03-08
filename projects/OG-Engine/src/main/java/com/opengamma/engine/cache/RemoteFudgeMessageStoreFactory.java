/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

/**
 * Creates {@link RemoteFudgeMessageStore} clients to connect to a {@link FudgeMessageStoreServer}.
 */
public class RemoteFudgeMessageStoreFactory implements FudgeMessageStoreFactory {

  private final RemoteCacheClient _client;

  public RemoteFudgeMessageStoreFactory(final RemoteCacheClient client) {
    _client = client;
  }

  protected RemoteCacheClient getRemoteCacheClient() {
    return _client;
  }

  @Override
  public FudgeMessageStore createMessageStore(final ViewComputationCacheKey cacheKey) {
    return new RemoteFudgeMessageStore(getRemoteCacheClient(), cacheKey);
  }

}
