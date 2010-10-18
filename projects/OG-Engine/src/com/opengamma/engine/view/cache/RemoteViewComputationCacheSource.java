/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.CacheMessageVisitor;
import com.opengamma.engine.view.cache.msg.ReleaseCacheMessage;
import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Caching client for {@link ViewComputationCacheServer}.
 */
public class RemoteViewComputationCacheSource extends DefaultViewComputationCacheSource implements FudgeMessageReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewComputationCacheSource.class);

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final CacheManager cacheManager) {
    this(client, privateDataStoreFactory, client.getFudgeContext(), cacheManager);
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final CacheManager cacheManager, final int maxLocalCachedElements) {
    this(client, privateDataStoreFactory, client.getFudgeContext(), cacheManager, maxLocalCachedElements);
  }

  /**
   * @param client the connection to a {@link ViewComputationCacheServer}
   * @param privateDataStoreFactory the private data store - the shared data store will be the remote one
   * @param fudgeContext the Fudge context the {@link DefaultViewComputationCache} will use for object encoding. This may be the same as the
   *                     one attached to the client's transport or different.
   * @param cacheManager the EH cache manager to use for the remote binary data store
   */
  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final FudgeContext fudgeContext, final CacheManager cacheManager) {
    super(createIdentifierMap(client), fudgeContext, privateDataStoreFactory, createDataStoreFactory(client, cacheManager, -1));
    client.setAsynchronousMessageReceiver(this);
  }

  public RemoteViewComputationCacheSource(final RemoteCacheClient client, final BinaryDataStoreFactory privateDataStoreFactory, final FudgeContext fudgeContext, final CacheManager cacheManager,
      final int maxLocalCachedElements) {
    super(createIdentifierMap(client), fudgeContext, privateDataStoreFactory, createDataStoreFactory(client, cacheManager, maxLocalCachedElements));
    client.setAsynchronousMessageReceiver(this);
  }

  private static IdentifierMap createIdentifierMap(final RemoteCacheClient client) {
    return new CachingIdentifierMap(new RemoteIdentifierMap(client));
  }

  private static BinaryDataStoreFactory createDataStoreFactory(final RemoteCacheClient client, final CacheManager cacheManager, final int maxLocalCachedElements) {
    final RemoteBinaryDataStoreFactory remote = new RemoteBinaryDataStoreFactory(client);
    if (maxLocalCachedElements >= 0) {
      return new CachingBinaryDataStoreFactory(remote, cacheManager, maxLocalCachedElements);
    } else {
      return new CachingBinaryDataStoreFactory(remote, cacheManager);
    }
  }

  private final CacheMessageVisitor _messageReceiver = new CacheMessageVisitor() {

    @Override
    protected CacheMessage visitReleaseCacheMessage(final ReleaseCacheMessage message) {
      s_logger.debug("Releasing cache {}/{}", message.getViewName(), message.getTimestamp());
      releaseCaches(message.getViewName(), message.getTimestamp());
      return null;
    }

    @Override
    protected <T extends CacheMessage> T visitUnexpectedMessage(final CacheMessage message) {
      s_logger.warn("Unexpected message {}", message);
      return null;
    }

  };

  @Override
  public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(fudgeContext);
    final CacheMessage message = dctx.fudgeMsgToObject(CacheMessage.class, msgEnvelope.getMessage());
    message.accept(_messageReceiver);
  }

}
