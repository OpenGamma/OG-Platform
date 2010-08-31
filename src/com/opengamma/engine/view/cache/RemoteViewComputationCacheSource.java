/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;

/**
 * Caching client for {@link ViewComputationCacheServer}. This is equivalent to constructing a {@link DefaultViewComputationCacheSource}
 * with a {@link RemoteIdentifierMap} wrapped in a {@link CachingIdentifierMap} and a {@link RemoteBinaryDataStore} wrapped in a
 * {@link CachingBinaryDataStore}.
 */
public class RemoteViewComputationCacheSource extends DefaultViewComputationCacheSource {

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory) {
    this(client, privateDataStoreFactory, client.getFudgeContext());
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final int maxLocalCachedElements) {
    this(client, privateDataStoreFactory, client.getFudgeContext(), maxLocalCachedElements);
  }

  /**
   * @param client the connection to a {@link ViewComputationCacheServer}
   * @param privateDataStoreFactory the private data store - the shared data store will be the remote one
   * @param fudgeContext the Fudge context the {@link DefaultViewComputationCache} will use for object encoding. This may be the same as the
   *                     one attached to the client's transport or different.
   */
  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final FudgeContext fudgeContext) {
    super(createIdentifierMap(client), fudgeContext, privateDataStoreFactory, createDataStoreFactory(client, -1));
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final FudgeContext fudgeContext, final int maxLocalCachedElements) {
    super(createIdentifierMap(client), fudgeContext, privateDataStoreFactory, createDataStoreFactory(client, maxLocalCachedElements));
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
