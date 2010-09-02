/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * The underlying is the shared data store component of a {@link DefaultViewComputationCache}.
 */
public class BinaryDataStoreServer implements FudgeRequestReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(BinaryDataStoreServer.class);
  private static final byte[] EMPTY_ARRAY = new byte[0];

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
    return getUnderlyingCache(request).getSharedDataStore();
  }

  protected void handleDelete(final DeleteRequest request) {
    getUnderlyingDataStore(request).delete();
  }

  protected GetResponse handleGet(final GetRequest request) {
    final List<Long> identifiers = request.getIdentifier();
    final Collection<byte[]> response;
    if (identifiers.size() == 1) {
      byte[] data = getUnderlyingDataStore(request).get(identifiers.get(0));
      if (data == null) {
        data = EMPTY_ARRAY;
      }
      response = Collections.singleton(data);
    } else {
      response = new ArrayList<byte[]>(identifiers.size());
      final Map<Long, byte[]> data = getUnderlyingDataStore(request).get(identifiers);
      for (Long identifier : identifiers) {
        byte[] value = data.get(identifier);
        if (value == null) {
          value = EMPTY_ARRAY;
        }
        response.add(value);
      }
    }
    return new GetResponse(response);
  }

  protected void handlePut(final PutRequest request) {
    final List<Long> identifiers = request.getIdentifier();
    final List<byte[]> data = request.getData();
    if (identifiers.size() == 1) {
      getUnderlyingDataStore(request).put(identifiers.get(0), data.get(0));
    } else {
      final Map<Long, byte[]> map = new HashMap<Long, byte[]>();
      final Iterator<Long> i = identifiers.iterator();
      final Iterator<byte[]> j = data.iterator();
      while (i.hasNext()) {
        map.put(i.next(), j.next());
      }
      getUnderlyingDataStore(request).put(map);
    }
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
    // FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), BinaryDataStoreResponse.class);
    return responseMsg;
  }

}
