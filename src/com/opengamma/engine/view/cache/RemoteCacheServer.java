/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * 
 *
 * @author kirk
 */
public class RemoteCacheServer implements FudgeRequestReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheServer.class);
  private final AtomicLong _nextValueSpecificationId = new AtomicLong(1l);
  private final ConcurrentMap<ValueSpecification, Long> _valueSpecificationIds =
    new ConcurrentHashMap<ValueSpecification, Long>();
  private final ConcurrentMap<ViewComputationCacheKey, Map<Long, ComputedValue>> _values =
    new ConcurrentHashMap<ViewComputationCacheKey, Map<Long, ComputedValue>>();

  @Override
  public FudgeFieldContainer requestReceived(
      FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    s_logger.debug("Got request {}", requestEnvelope.getMessage());
    Object request = context.fudgeMsgToObject(requestEnvelope.getMessage());
    Object response = null;
    if(request instanceof ValueSpecificationLookupRequest) {
      response = handleValueSpecificationLookupRequest(context.getFudgeContext(), (ValueSpecificationLookupRequest) request);
    } else if(request instanceof ValueLookupRequest) {
      response = handleValueLookupRequest(context.getFudgeContext(), (ValueLookupRequest) request);
    } else if(request instanceof ValuePutRequest) {
      response = handleValuePutRequest(context.getFudgeContext(), (ValuePutRequest) request);
    } else {
      s_logger.warn("Got an unhandled request type: {}", request);
    }
    if(response == null) {
      return context.getFudgeContext().newMessage();
    } else {
      FudgeSerializationContext ctx = new FudgeSerializationContext(context.getFudgeContext());
      MutableFudgeFieldContainer responseMsg = ctx.objectToFudgeMsg(response);
      FudgeSerializationContext.addClassHeader (responseMsg, response.getClass());
      return responseMsg;
    }
  }

  /**
   * @param request
   * @return
   */
  private Object handleValueSpecificationLookupRequest(
      FudgeContext context,
      ValueSpecificationLookupRequest request) {
    Long currentValue = _valueSpecificationIds.get(request.getRequest());
    if(currentValue == null) {
      Long newValue = _nextValueSpecificationId.getAndIncrement();
      currentValue = _valueSpecificationIds.putIfAbsent(request.getRequest(), newValue);
      if(currentValue == null) {
        s_logger.info("Allocated new ValueSpecification ID {} for {}", newValue, request.getRequest());
        currentValue = newValue;
      }
    }
    ValueSpecificationLookupResponse response = new ValueSpecificationLookupResponse(request.getCorrelationId(), request.getRequest(), currentValue);
    return response;
  }

  /**
   * @param fudgeContext
   * @param request
   * @return
   */
  private Object handleValueLookupRequest(FudgeContext fudgeContext,
      ValueLookupRequest request) {
    ViewComputationCacheKey cacheKey = new ViewComputationCacheKey(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshot());
    Map<Long, ComputedValue> cacheMap = _values.get(cacheKey);
    ComputedValue computedValue = null;
    if(cacheMap != null) {
      computedValue = cacheMap.get(request.getSpecificationId());
    }
    ValueLookupResponse lookupResponse = new ValueLookupResponse(request.getCorrelationId(), computedValue);
    return lookupResponse;
  }

  /**
   * @param fudgeContext
   * @param request
   * @return
   */
  private Object handleValuePutRequest(FudgeContext fudgeContext,
      ValuePutRequest request) {
    ViewComputationCacheKey cacheKey = new ViewComputationCacheKey(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshot());
    Map<Long, ComputedValue> cacheMap = _values.get(cacheKey);
    if(cacheMap == null) {
      Map<Long, ComputedValue> freshMap = new ConcurrentHashMap<Long, ComputedValue>();
      cacheMap = _values.putIfAbsent(cacheKey, freshMap);
      if(cacheMap == null) {
        cacheMap = freshMap;
      }
    }
    cacheMap.put(request.getSpecificationId(), request.getValue());
    ValuePutResponse putResponse = new ValuePutResponse(request.getCorrelationId());
    return putResponse;
  }

}
