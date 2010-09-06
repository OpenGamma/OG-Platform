/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.msg.BinaryDataStoreRequest;
import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.IdentifierMapRequest;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Composite server class for dispatching calls to a {@link IdentifierMapServer} and 
 * {@link BinaryDataStoreServer} within the same JVM.
 */
public class ViewComputationCacheServer implements FudgeConnectionReceiver, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationCacheServer.class);

  private final IdentifierMapServer _identifierMap;
  private final BinaryDataStoreServer _binaryDataStore;

  public ViewComputationCacheServer(final IdentifierMapServer identifierMap, final BinaryDataStoreServer binaryDataStore) {
    _identifierMap = identifierMap;
    _binaryDataStore = binaryDataStore;
  }

  public ViewComputationCacheServer(final DefaultViewComputationCacheSource cacheSource) {
    this(new IdentifierMapServer(cacheSource.getIdentifierMap()), new BinaryDataStoreServer(cacheSource));
  }

  protected IdentifierMapServer getIdentifierMap() {
    return _identifierMap;
  }

  protected BinaryDataStoreServer getBinaryDataStore() {
    return _binaryDataStore;
  }

  /**
   * Handles the message.
   * 
   * @param message the message
   * @param connection the connection
   * @return the response, not {@code null}
   */
  protected CacheMessage handleCacheMessage(final CacheMessage message, final FudgeConnection connection) {
    CacheMessage response = null;
    if (message instanceof BinaryDataStoreRequest) {
      response = getBinaryDataStore().handleBinaryDataStoreRequest((BinaryDataStoreRequest) message, connection);
    } else if (message instanceof IdentifierMapRequest) {
      response = getIdentifierMap().handleIdentifierMapRequest((IdentifierMapRequest) message);
    } else {
      s_logger.warn("Unexpected message - {}", message);
    }
    if (response == null) {
      response = new CacheMessage();
    }
    return response;
  }

  protected void handleMessage(final FudgeConnection connection, final FudgeContext fudgeContext, final FudgeMsgEnvelope message) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(fudgeContext);
    final CacheMessage request = dctx.fudgeMsgToObject(CacheMessage.class, message.getMessage());
    final CacheMessage response = handleCacheMessage(request, connection);
    if (response != null) {
      response.setCorrelationId(request.getCorrelationId());
      final FudgeSerializationContext sctx = new FudgeSerializationContext(fudgeContext);
      final MutableFudgeFieldContainer responseMsg = sctx.objectToFudgeMsg(response);
      // We have only one response type for each request, so don't really need the headers
      // FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), CacheMessage.class);
      connection.getFudgeMessageSender().send(responseMsg);
    }
  }

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    handleMessage(connection, fudgeContext, message);
    getBinaryDataStore().onNewConnection(connection);
    connection.setFudgeMessageReceiver(new FudgeMessageReceiver() {

      @Override
      public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message) {
        handleMessage(connection, fudgeContext, message);
      }

    });
  }

  @Override
  public void connectionFailed(final FudgeConnection connection, Exception cause) {
    getBinaryDataStore().onDroppedConnection(connection);
  }

  @Override
  public void connectionReset(final FudgeConnection connection) {
    // Shouldn't happen
  }

}
