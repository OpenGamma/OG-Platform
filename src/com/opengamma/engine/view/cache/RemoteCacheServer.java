/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeRequestReceiver;

// REVIEW kirk 2010-03-31 -- This is a candidate for movement into com.og.u.transport
// per the initial work on UTL-26 if we can come up with a better (non-Closure) way to
// handle the dispatches.
/**
 * 
 *
 */
public class RemoteCacheServer implements FudgeRequestReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheServer.class);

  private final AbstractViewComputationCacheSource _underlying;

  public RemoteCacheServer(final AbstractViewComputationCacheSource underlying) {
    _underlying = underlying;
  }

  @Override
  public FudgeFieldContainer requestReceived(final FudgeDeserializationContext context, final FudgeMsgEnvelope requestEnvelope) {
    s_logger.debug("Got request {}", requestEnvelope.getMessage());
    final RemoteCacheMessage request = context.fudgeMsgToObject(RemoteCacheMessage.class, requestEnvelope.getMessage());
    final FudgeContext fudgeContext = context.getFudgeContext();
    RemoteCacheMessage response = null;
    if (request instanceof ValueSpecificationLookupRequest) {
      response = handleValueSpecificationLookupRequest(fudgeContext, (ValueSpecificationLookupRequest) request);
    } else if (request instanceof ValueLookupRequest) {
      response = handleValueLookupRequest(fudgeContext, (ValueLookupRequest) request);
    } else if (request instanceof ValuePutRequest) {
      response = handleValuePutRequest(fudgeContext, (ValuePutRequest) request);
    } else if (request instanceof CachePurgeRequest) {
      response = handleCachePurgeRequest(fudgeContext, (CachePurgeRequest) request);
    } else {
      s_logger.warn("Got an unhandled request type: {}", request);
    }
    if (response == null) {
      s_logger.debug("Creating dummy message to unblock the caller and let them error");
      response = new RemoteCacheMessage(request.getCorrelationId());
    } else {
      response.setCorrelationId(request.getCorrelationId());
    }
    final FudgeSerializationContext ctx = new FudgeSerializationContext(fudgeContext);
    final MutableFudgeFieldContainer responseMsg = ctx.objectToFudgeMsg(response);
    FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), RemoteCacheMessage.class);
    return responseMsg;
  }

  protected AbstractViewComputationCacheSource getUnderlying() {
    return _underlying;
  }

  /**
   * @param request
   * @return
   */
  private ValueSpecificationLookupResponse handleValueSpecificationLookupRequest(final FudgeContext context, final ValueSpecificationLookupRequest request) {
    final long specificationId = getUnderlying().getIdentifierSource().getIdentifier(request.getSpecification());
    final ValueSpecificationLookupResponse response = new ValueSpecificationLookupResponse(specificationId);
    return response;
  }

  /**
   * @param fudgeContext
   * @param request
   * @return
   */
  private ValueLookupResponse handleValueLookupRequest(FudgeContext fudgeContext, ValueLookupRequest request) {
    final Object value = getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshot()).getValue(request.getSpecificationId());
    final ValueLookupResponse response = new ValueLookupResponse();
    response.setValue(value);
    return response;
  }

  /**
   * @param fudgeContext
   * @param request
   * @return
   */
  private ValuePutResponse handleValuePutRequest(FudgeContext fudgeContext, ValuePutRequest request) {
    getUnderlying().getCache(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshot()).putValue(request.getSpecificationId(), request.getValue());
    final ValuePutResponse response = new ValuePutResponse();
    return response;
  }

  /**
   * @param fudgeContext
   * @param request
   * @return
   */
  private CachePurgeResponse handleCachePurgeRequest(FudgeContext fudgeContext, CachePurgeRequest request) {
    s_logger.info("Purging on request {}", request);
    getUnderlying().releaseCaches(request.getViewName(), request.getSnapshot());
    final CachePurgeResponse purgeResponse = new CachePurgeResponse();
    return purgeResponse;
  }

}
