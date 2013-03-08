/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.msg.CacheMessage;
import com.opengamma.engine.cache.msg.CacheMessageVisitor;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Composite server class for dispatching calls to a {@link IdentifierMapServer} and 
 * {@link FudgeMessageStoreServer} within the same JVM.
 */
public class ViewComputationCacheServer implements FudgeConnectionReceiver, FudgeConnectionStateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationCacheServer.class);

  private final IdentifierMapServer _identifierMap;
  private final FudgeMessageStoreServer _binaryDataStore;

  public ViewComputationCacheServer(final IdentifierMapServer identifierMap, final FudgeMessageStoreServer binaryDataStore) {
    _identifierMap = identifierMap;
    _binaryDataStore = binaryDataStore;
  }

  public ViewComputationCacheServer(final DefaultViewComputationCacheSource cacheSource) {
    this(new IdentifierMapServer(cacheSource.getIdentifierMap()), new FudgeMessageStoreServer(cacheSource));
  }

  protected IdentifierMapServer getIdentifierMap() {
    return _identifierMap;
  }

  protected FudgeMessageStoreServer getBinaryDataStore() {
    return _binaryDataStore;
  }

  private class MessageHandler extends CacheMessageVisitor implements FudgeMessageReceiver {

    private final FudgeConnection _connection;
    private final CacheMessageVisitor _binaryDataStore;

    public MessageHandler(final FudgeConnection connection) {
      _connection = connection;
      _binaryDataStore = ViewComputationCacheServer.this.getBinaryDataStore().onNewConnection(connection);
    }

    private CacheMessageVisitor getBinaryDataStore() {
      return _binaryDataStore;
    }

    private FudgeConnection getConnection() {
      return _connection;
    }

    @Override
    protected <T extends CacheMessage> T visitUnexpectedMessage(final CacheMessage message) {
      s_logger.warn("Unexpected message - {}", message);
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends CacheMessage> T visitBinaryDataStoreMessage(final CacheMessage message) {
      return (T) message.accept(getBinaryDataStore());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends CacheMessage> T visitIdentifierMapMessage(final CacheMessage message) {
      return (T) message.accept(getIdentifierMap());
    }

    @Override
    public void messageReceived(final FudgeContext context, final FudgeMsgEnvelope message) {
      final FudgeDeserializer deserializer = new FudgeDeserializer(context);
      final CacheMessage request = deserializer.fudgeMsgToObject(CacheMessage.class, message.getMessage());
      CacheMessage response = request.accept(this);
      if (response == null) {
        if (request.getCorrelationId() != null) {
          response = new CacheMessage();
        }
      }
      if (response != null) {
        response.setCorrelationId(request.getCorrelationId());
        final FudgeSerializer sctx = new FudgeSerializer(context);
        final MutableFudgeMsg responseMsg = sctx.objectToFudgeMsg(response);
        // We have only one response type for each request, so don't really need the headers
        // FudgeSerializer.addClassHeader(responseMsg, response.getClass(), CacheMessage.class);
        getConnection().getFudgeMessageSender().send(responseMsg);
      }
    }

  };

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    connection.setConnectionStateListener(this);
    final MessageHandler handler = new MessageHandler(connection);
    handler.messageReceived(fudgeContext, message);
    connection.setFudgeMessageReceiver(handler);
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
