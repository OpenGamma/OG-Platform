/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cache.msg.BinaryDataStoreRequest;
import com.opengamma.engine.view.cache.msg.BinaryDataStoreResponse;
import com.opengamma.engine.view.cache.msg.DeleteRequest;
import com.opengamma.engine.view.cache.msg.GetRequest;
import com.opengamma.engine.view.cache.msg.GetResponse;
import com.opengamma.engine.view.cache.msg.PutRequest;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * Server for {@link RemoteBinaryDataStore} clients created by a {@link RemoteBinaryDataStoreFactory}.
 */
public class BinaryDataStoreServer implements FudgeRequestReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(BinaryDataStoreServer.class);

  private final DefaultViewComputationCacheSource _underlying;

  public BinaryDataStoreServer(final DefaultViewComputationCacheSource underlying) {
    _underlying = underlying;
  }

  protected DefaultViewComputationCacheSource getUnderlying() {
    return _underlying;
  }

  protected DefaultViewComputationCache getUnderlyingCache(final BinaryDataStoreRequest request) {
    return getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshotTimestamp());
  }

  protected BinaryDataStore getUnderlyingDataStore(final BinaryDataStoreRequest request) {
    return getUnderlyingCache(request).getDataStore();
  }

  protected void handleDelete(final DeleteRequest request) {
    getUnderlyingDataStore(request).delete();
  }

  protected GetResponse handleGet(final GetRequest request) {
    final GetResponse response = new GetResponse();
    response.setData(getUnderlyingDataStore(request).get(request.getIdentifier()));
    return response;
  }

  protected void handlePut(final PutRequest request) {
    getUnderlyingDataStore(request).put(request.getIdentifier(), request.getData());
  }

  /**
   * Handles the request. 
   * 
   * @param request the request
   * @return not {@code null}
   */
  protected BinaryDataStoreResponse handleBinaryDataStoreRequest(final BinaryDataStoreRequest request) {
    BinaryDataStoreResponse response = null;
    if (request instanceof GetRequest) {
      response = handleGet((GetRequest) request);
    } else if (request instanceof PutRequest) {
      handlePut((PutRequest) request);
    } else if (request instanceof DeleteRequest) {
      handleDelete((DeleteRequest) request);
    } else {
      s_logger.warn("Unexpected message - {}", request);
    }
    if (response == null) {
      response = new BinaryDataStoreResponse();
    }
    return response;
  }

  @Override
  public FudgeFieldContainer requestReceived(final FudgeDeserializationContext context, final FudgeMsgEnvelope requestEnvelope) {
    final BinaryDataStoreRequest request = context.fudgeMsgToObject(BinaryDataStoreRequest.class, requestEnvelope.getMessage());
    final FudgeContext fudgeContext = context.getFudgeContext();
    final BinaryDataStoreResponse response = handleBinaryDataStoreRequest(request);
    response.setCorrelationId(request.getCorrelationId());
    final FudgeSerializationContext ctx = new FudgeSerializationContext(fudgeContext);
    final MutableFudgeFieldContainer responseMsg = ctx.objectToFudgeMsg(response);
    // We have only one response for each request type, so don't really need the headers
    //FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), BinaryDataStoreResponse.class);
    return responseMsg;
  }

}
