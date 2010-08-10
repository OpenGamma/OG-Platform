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
  private final AtomicLong _nextValueSpecificationId = new AtomicLong(1L);
  private final ConcurrentMap<ValueSpecification, Long> _valueSpecificationIds =
    new ConcurrentHashMap<ValueSpecification, Long>();
  private final ConcurrentMap<ViewComputationCacheKey, Map<Long, Object>> _values =
    new ConcurrentHashMap<ViewComputationCacheKey, Map<Long, Object>>();
  private final Lock _purgeLock = new ReentrantLock();

  @Override
  public FudgeFieldContainer requestReceived(
      FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    s_logger.debug("Got request {}", requestEnvelope.getMessage());
    RemoteCacheMessage request = context.fudgeMsgToObject(RemoteCacheMessage.class, requestEnvelope.getMessage());
    RemoteCacheMessage response = null;
    if (request instanceof ValueSpecificationLookupRequest) {
      response = handleValueSpecificationLookupRequest(context.getFudgeContext(), (ValueSpecificationLookupRequest) request);
    } else if (request instanceof ValueLookupRequest) {
      response = handleValueLookupRequest(context.getFudgeContext(), (ValueLookupRequest) request);
    } else if (request instanceof ValuePutRequest) {
      response = handleValuePutRequest(context.getFudgeContext(), (ValuePutRequest) request);
    } else if (request instanceof CachePurgeRequest) {
      response = handleCachePurgeRequest(context.getFudgeContext(), (CachePurgeRequest) request);
    } else {
      s_logger.warn("Got an unhandled request type: {}", request);
    }
    if (response == null) {
      return FudgeContext.EMPTY_MESSAGE;
    } else {
      FudgeSerializationContext ctx = new FudgeSerializationContext(context.getFudgeContext());
      MutableFudgeFieldContainer responseMsg = ctx.objectToFudgeMsg(response);
      FudgeSerializationContext.addClassHeader(responseMsg, response.getClass(), RemoteCacheMessage.class);
      return responseMsg;
    }
  }

  /**
   * @param request
   * @return
   */
  private ValueSpecificationLookupResponse handleValueSpecificationLookupRequest(
      FudgeContext context,
      ValueSpecificationLookupRequest request) {
    Long currentValue = _valueSpecificationIds.get(request.getRequest());
    if (currentValue == null) {
      Long newValue = _nextValueSpecificationId.getAndIncrement();
      currentValue = _valueSpecificationIds.putIfAbsent(request.getRequest(), newValue);
      if (currentValue == null) {
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
  private ValueLookupResponse handleValueLookupRequest(FudgeContext fudgeContext,
      ValueLookupRequest request) {
    ViewComputationCacheKey cacheKey = new ViewComputationCacheKey(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshot());
    Map<Long, Object> cacheMap = _values.get(cacheKey);
    Object computedValue = null;
    if (cacheMap != null) {
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
  private ValuePutResponse handleValuePutRequest(FudgeContext fudgeContext,
      ValuePutRequest request) {
    ViewComputationCacheKey cacheKey = new ViewComputationCacheKey(request.getViewName(), request.getCalculationConfigurationName(), request.getSnapshot());
    Map<Long, Object> cacheMap = _values.get(cacheKey);
    if (cacheMap == null) {
      Map<Long, Object> freshMap = new ConcurrentHashMap<Long, Object>();
      cacheMap = _values.putIfAbsent(cacheKey, freshMap);
      if (cacheMap == null) {
        cacheMap = freshMap;
      }
    }
    cacheMap.put(request.getSpecificationId(), request.getValue());
    ValuePutResponse putResponse = new ValuePutResponse(request.getCorrelationId());
    return putResponse;
  }

  /**
   * @param fudgeContext
   * @param request
   * @return
   */
  private CachePurgeResponse handleCachePurgeRequest(FudgeContext fudgeContext,
      CachePurgeRequest request) {
    s_logger.info("Purging on request {}", request);
    int nPurged = 0;
    _purgeLock.lock();
    try {
      Iterator<Map.Entry<ViewComputationCacheKey, Map<Long, Object>>> entryIter = _values.entrySet().iterator();
      while (entryIter.hasNext()) {
        Map.Entry<ViewComputationCacheKey, Map<Long, Object>> entry = entryIter.next();
        ViewComputationCacheKey cacheKey = entry.getKey();
        if (!ObjectUtils.equals(request.getViewName(), cacheKey.getViewName())) {
          continue;
        }
        if (cacheKey.getSnapshotTimestamp() != request.getSnapshot()) {
          continue;
        }
        if (request.getCalculationConfigurationName() != null) {
          if (!ObjectUtils.equals(request.getCalculationConfigurationName(), cacheKey.getCalculationConfigurationName())) {
            continue;
          }
        }
        entryIter.remove();
        nPurged++;
      }
    } finally {
      _purgeLock.unlock();
    }
    CachePurgeResponse purgeResponse = new CachePurgeResponse(request.getCorrelationId(), nPurged);
    return purgeResponse;
  }

}
