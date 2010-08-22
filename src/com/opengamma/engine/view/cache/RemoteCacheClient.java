/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.cache.msg.BinaryDataStoreRequest;
import com.opengamma.engine.view.cache.msg.BinaryDataStoreResponse;
import com.opengamma.engine.view.cache.msg.CacheMessage;
import com.opengamma.engine.view.cache.msg.IdentifierMapRequest;
import com.opengamma.engine.view.cache.msg.IdentifierMapResponse;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.FudgeSynchronousClient;

/**
 * {@link FudgeSynchronousClient} implementation for the remote cache component clients.
 */
public class RemoteCacheClient extends FudgeSynchronousClient {

  /**
   * Creates a new client.
   * 
   * @param requestSender the underlying transport
   */
  public RemoteCacheClient(FudgeRequestSender requestSender) {
    super(requestSender);
  }

  protected <Request extends CacheMessage, Response extends CacheMessage> Response sendMessage(final Request request, final Class<Request> requestClass, final Class<Response> responseClass) {
    final FudgeSerializationContext scontext = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    final long correlationId = getNextCorrelationId();
    request.setCorrelationId(correlationId);
    final FudgeFieldContainer responseMsg = sendRequestAndWaitForResponse(
        FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(request), request.getClass(), requestClass), correlationId);
    final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(getRequestSender().getFudgeContext());
    final Response response = dcontext.fudgeMsgToObject(responseClass, responseMsg);
    return response;
  }

  protected <T extends IdentifierMapResponse> T sendMessage(final IdentifierMapRequest request, final Class<T> expectedResponse) {
    return sendMessage(request, IdentifierMapRequest.class, expectedResponse);
  }

  protected <T extends BinaryDataStoreResponse> T sendMessage(final BinaryDataStoreRequest request, final Class<T> expectedResponse) {
    return sendMessage(request, BinaryDataStoreRequest.class, expectedResponse);
  }

  @Override
  protected long getCorrelationIdFromReply(final FudgeFieldContainer reply) {
    return reply.getLong(CacheMessage.CORRELATION_ID_KEY);
  }

}
