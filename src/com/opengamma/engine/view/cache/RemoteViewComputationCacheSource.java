/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.ReleaseCacheMessage;
import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Caching client for {@link ViewComputationCacheServer}.
 */
public class RemoteViewComputationCacheSource extends DefaultViewComputationCacheSource implements FudgeMessageReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewComputationCacheSource.class);

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
    client.setAsynchronousMessageReceiver(this);
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final FudgeContext fudgeContext, final int maxLocalCachedElements) {
    super(createIdentifierMap(client), fudgeContext, privateDataStoreFactory, createDataStoreFactory(client, maxLocalCachedElements));
    client.setAsynchronousMessageReceiver(this);
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

  protected void handleReleaseCache(final ReleaseCacheMessage message) {
    s_logger.debug("Releasing cache {}/{}", message.getViewName(), message.getTimestamp());
    releaseCaches(message.getViewName(), message.getTimestamp());
  }

  @Override
  public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(fudgeContext);
    final CacheMessage message = dctx.fudgeMsgToObject(CacheMessage.class, msgEnvelope.getMessage());
    if (message instanceof ReleaseCacheMessage) {
      handleReleaseCache((ReleaseCacheMessage) message);
    } else {
      s_logger.warn("Unexpected message {}", message);
    }
  }

}
