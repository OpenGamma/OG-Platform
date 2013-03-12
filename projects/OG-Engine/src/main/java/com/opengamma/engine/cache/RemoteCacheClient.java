/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.cache.msg.CacheMessage;
import com.opengamma.engine.cache.msg.SlaveChannelMessage;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeSynchronousClient;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link FudgeSynchronousClient} implementation for the remote cache component clients. The client
 * has a "get" and "put" channel. Although equal priority, this gives two blocking queues to isolate
 * operations that query the cache from those that update or control it. This allows, for example,
 * cache writes from a previous job to not delay loads needed by the next job.
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
    protected Long getCorrelationIdFromReply(final FudgeMsg reply) {
      return reply.getLong(CacheMessage.CORRELATION_ID_KEY);
    }

    private <Request extends CacheMessage, Response extends CacheMessage> Response sendMessage(final Request request, final Class<Response> responseClass) {
      final FudgeSerializer scontext = new FudgeSerializer(getMessageSender().getFudgeContext());
      final long correlationId = getNextCorrelationId();
      request.setCorrelationId(correlationId);
      final FudgeMsg responseMsg = sendRequestAndWaitForResponse(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(request), request.getClass(), CacheMessage.class),
          correlationId);
      final FudgeDeserializer dcontext = new FudgeDeserializer(getMessageSender().getFudgeContext());
      final Response response = dcontext.fudgeMsgToObject(responseClass, responseMsg);
      return response;
    }

    private <Message extends CacheMessage> void postMessage(final Message message) {
      final FudgeSerializer scontext = new FudgeSerializer(getMessageSender().getFudgeContext());
      sendMessage(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(message), message.getClass(), CacheMessage.class));
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
      _fudgeGets.postMessage(new SlaveChannelMessage());
      _fudgePuts = new FudgeClient(requestPuts);
    } else {
      _fudgePuts = _fudgeGets;
    }
  }

  protected void setAsynchronousMessageReceiver(final FudgeMessageReceiver asynchronousMessageReceiver) {
    _fudgePuts.setAsynchronousMessageReceiver(asynchronousMessageReceiver);
  }

  protected <T extends CacheMessage> T sendGetMessage(final CacheMessage request, final Class<T> expectedResponse) {
    return _fudgeGets.sendMessage(request, expectedResponse);
  }

  protected <T extends CacheMessage> T sendPutMessage(final CacheMessage request, final Class<T> expectedResponse) {
    return _fudgePuts.sendMessage(request, expectedResponse);
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeGets.getMessageSender().getFudgeContext();
  }

}
