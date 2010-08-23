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
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.FudgeSynchronousClient;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link FudgeSynchronousClient} implementation for the remote cache component clients.
 */
public class RemoteCacheClient {

  private static class FudgeClient extends FudgeSynchronousClient {

    /**
     * @param requestSender
     */
    protected FudgeClient(final FudgeRequestSender requestSender) {
      super(requestSender);
    }

    @Override
    protected long getCorrelationIdFromReply(final FudgeFieldContainer reply) {
      return reply.getLong(CacheMessage.CORRELATION_ID_KEY);
    }

    private <Request extends CacheMessage, Response extends CacheMessage> Response sendMessage(final Request request, final Class<Request> requestClass, final Class<Response> responseClass) {
      final FudgeSerializationContext scontext = new FudgeSerializationContext(getRequestSender().getFudgeContext());
      final long correlationId = getNextCorrelationId();
      request.setCorrelationId(correlationId);
      final FudgeFieldContainer responseMsg = sendRequestAndWaitForResponse(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(request), request.getClass(), requestClass),
          correlationId);
      final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(getRequestSender().getFudgeContext());
      final Response response = dcontext.fudgeMsgToObject(responseClass, responseMsg);
      return response;
    }

  }

  private final FudgeClient _fudgeGets;
  private final FudgeClient _fudgePuts;

  /**
   * Creates a new client using a single underlying transport.
   * 
   * @param requestSender the underlying transport
   */
  public RemoteCacheClient(final FudgeRequestSender requestSender) {
    ArgumentChecker.notNull(requestSender, "requestSender");
    _fudgeGets = new FudgeClient(requestSender);
    _fudgePuts = _fudgeGets;
  }

  /**
   * Creates a new client using a transport pair, one for cache "get" operations and one for "put" operations.
   * 
   * @param requestGets get operations
   * @param requestPuts put operations
   */
  public RemoteCacheClient(final FudgeRequestSender requestGets, final FudgeRequestSender requestPuts) {
    ArgumentChecker.notNull(requestGets, "requestPrioritySender");
    ArgumentChecker.notNull(requestPuts, "requestSender");
    _fudgeGets = new FudgeClient(requestGets);
    if (requestPuts != requestGets) {
      _fudgePuts = new FudgeClient(requestPuts);
    } else {
      _fudgePuts = _fudgeGets;
    }
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
    return _fudgeGets.getRequestSender().getFudgeContext();
  }

}
