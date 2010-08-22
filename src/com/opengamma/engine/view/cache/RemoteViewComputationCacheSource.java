/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;

import com.opengamma.transport.FudgeRequestSender;

/**
 * Caching client for {@link ViewComputationCacheServer}. This is equivalent to constructing a {@link DefaultViewComputationCacheSource}
 * with a {@link RemoteIdentifierMap} wrapped in a {@link CachingIdentifierMap} and a {@link RemoteBinaryDataStore} wrapped in a
 * {@link CachingBinaryDataStore}. 
 */
public class RemoteViewComputationCacheSource extends DefaultViewComputationCacheSource {

  public RemoteViewComputationCacheSource(final FudgeRequestSender requestSender) {
    this(new RemoteCacheClient(requestSender));
  }

  public RemoteViewComputationCacheSource(final FudgeRequestSender requestSender, final int maxLocalCachedElements) {
    this(new RemoteCacheClient(requestSender), maxLocalCachedElements);
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client) {
    this(client, client.getRequestSender().getFudgeContext());
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final int maxLocalCachedElements) {
    this(client, client.getRequestSender().getFudgeContext(), maxLocalCachedElements);
  }

  /**
   * @param client the connection to a {@link ViewComputationCacheServer}
   * @param fudgeContext the Fudge context the {@link DefaultViewComputationCache} will use for object encoding. This may be the same as the
   *                     one attached to the client's transport or different.
   */
  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final FudgeContext fudgeContext) {
    super(createIdentifierMap(client), fudgeContext, createDataStoreFactory(client, -1));
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final FudgeContext fudgeContext, final int maxLocalCachedElements) {
    super(createIdentifierMap(client), fudgeContext, createDataStoreFactory(client, maxLocalCachedElements));
  }

  private static IdentifierMap createIdentifierMap(final RemoteCacheClient client) {
    return new CachingIdentifierMap(new RemoteIdentifierMap(client));
  }

  private static BinaryDataStoreFactory createDataStoreFactory(final RemoteCacheClient client, final int maxLocalCachedElements) {
    final RemoteBinaryDataStoreFactory remote = new RemoteBinaryDataStoreFactory(client);
    if (maxLocalCachedElements >= 0) {
      return new CachingBinaryDataStoreFactory(remote, maxLocalCachedElements);
    } else {
      return new CachingBinaryDataStoreFactory(remote);
    }
  }

}
