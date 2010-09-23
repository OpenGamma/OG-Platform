/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.cache.msg.BinaryDataStoreRequest;
import com.opengamma.engine.view.cache.msg.BinaryDataStoreResponse;
import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.IdentifierMapRequest;
import com.opengamma.engine.view.cache.msg.IdentifierMapResponse;
import com.opengamma.engine.view.cache.msg.SlaveChannelMessage;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeSynchronousClient;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link FudgeSynchronousClient} implementation for the remote cache component clients.
 */
public class RemoteCacheClient {

  private class FudgeClient extends FudgeSynchronousClient {

    /**
     * @param requestSender
     */
    protected FudgeClient(final FudgeConnection connection) {
      super(connection);
    }

    @Override
    protected Long getCorrelationIdFromReply(final FudgeFieldContainer reply) {
      return reply.getLong(CacheMessage.CORRELATION_ID_KEY);
    }

    private <Request extends CacheMessage, Response extends CacheMessage> Response sendMessage(final Request request, final Class<Request> requestClass, final Class<Response> responseClass) {
      final FudgeSerializationContext scontext = new FudgeSerializationContext(getMessageSender().getFudgeContext());
      final long correlationId = getNextCorrelationId();
      request.setCorrelationId(correlationId);
      final FudgeFieldContainer responseMsg = sendRequestAndWaitForResponse(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(request), request.getClass(), requestClass),
          correlationId);
      final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(getMessageSender().getFudgeContext());
      final Response response = dcontext.fudgeMsgToObject(responseClass, responseMsg);
      return response;
    }

    private <Message extends CacheMessage> void postMessage(final Message message, final Class<Message> messageClass) {
      final FudgeSerializationContext scontext = new FudgeSerializationContext(getMessageSender().getFudgeContext());
      sendMessage(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(message), message.getClass(), messageClass));
    }

  }

  private final FudgeClient _fudgeGets;
  private final FudgeClient _fudgePuts;

  /**
   * Creates a new client using a single underlying transport.
   * 
   * @param connection the underlying transport
   */
  public RemoteCacheClient(final FudgeConnection connection) {
    ArgumentChecker.notNull(connection, "connection");
    _fudgeGets = new FudgeClient(connection);
    _fudgePuts = _fudgeGets;
  }

  /**
   * Creates a new client using a transport pair, one for cache "get" operations and one for "put" operations.
   * 
   * @param requestGets get operations
   * @param requestPuts put operations
   */
  public RemoteCacheClient(final FudgeConnection requestGets, final FudgeConnection requestPuts) {
    ArgumentChecker.notNull(requestGets, "requestGets");
    ArgumentChecker.notNull(requestPuts, "requestPuts");
    _fudgeGets = new FudgeClient(requestGets);
    if (requestPuts != requestGets) {
      _fudgeGets.postMessage(new SlaveChannelMessage(), BinaryDataStoreRequest.class);
      _fudgePuts = new FudgeClient(requestPuts);
    } else {
      _fudgePuts = _fudgeGets;
    }
  }

  protected void setAsynchronousMessageReceiver(final FudgeMessageReceiver asynchronousMessageReceiver) {
    _fudgePuts.setAsynchronousMessageReceiver(asynchronousMessageReceiver);
  }

  protected <T extends IdentifierMapResponse> T sendGetMessage(final IdentifierMapRequest request, final Class<T> expectedResponse) {
    return _fudgeGets.sendMessage(request, IdentifierMapRequest.class, expectedResponse);
  }

  protected <T extends BinaryDataStoreResponse> T sendPutMessage(final BinaryDataStoreRequest request, final Class<T> expectedResponse) {
    return _fudgePuts.sendMessage(request, BinaryDataStoreRequest.class, expectedResponse);
  }

  protected <T extends BinaryDataStoreResponse> T sendGetMessage(final BinaryDataStoreRequest request, final Class<T> expectedResponse) {
    return _fudgeGets.sendMessage(request, BinaryDataStoreRequest.class, expectedResponse);
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeGets.getMessageSender().getFudgeContext();
  }

}
