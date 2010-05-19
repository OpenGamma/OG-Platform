/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.FudgeSynchronousClient;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class RemoteCacheClient extends FudgeSynchronousClient {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheClient.class);
  private final ConcurrentMap<ValueSpecification, Long> _specificationIds = new ConcurrentHashMap<ValueSpecification, Long>();
  
  public RemoteCacheClient(FudgeRequestSender requestSender) {
    super(requestSender);
  }

  public long getValueSpecificationId(ValueSpecification valueSpec) {
    ArgumentChecker.notNull(valueSpec, "Value Specification");
    Long result = _specificationIds.get(valueSpec);
    if(result != null) {
      return result;
    }
    result = remoteLookupValueSpecificationId(valueSpec);
    Long previousResult = _specificationIds.putIfAbsent(valueSpec, result);
    assert (previousResult == null) || (previousResult == result) : "Inconsistent results from concurrent spec requests!";
    return result;
  }

  protected long remoteLookupValueSpecificationId(ValueSpecification valueSpec) {
    ArgumentChecker.notNull(valueSpec, "Value Specification");
    long correlationId = getNextCorrelationId();
    s_logger.info("Requesting value specification ID for {} - Correlation {}", valueSpec, correlationId);
    ValueSpecificationLookupRequest request = new ValueSpecificationLookupRequest(correlationId, valueSpec);
    
    FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    MutableFudgeFieldContainer requestMsg = ctx.objectToFudgeMsg(request);
    FudgeSerializationContext.addClassHeader (requestMsg, ValueSpecificationLookupRequest.class);
    
    //FudgeFieldContainer requestMsg = request.toFudgeMsg(new FudgeSerializationContext(getRequestSender().getFudgeContext()));
    Object resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof ValueSpecificationLookupResponse;
    ValueSpecificationLookupResponse lookupResponse = (ValueSpecificationLookupResponse) resultValue;
    return lookupResponse.getResponse();
  }
  
  public Object getValue(String viewName, String calcConfigName, long timestamp, ValueSpecification valueSpecification) {
    // TODO kirk 2010-03-30 -- Check Inputs.
    
    long correlationId = getNextCorrelationId();
    s_logger.info("Requesting value {} - Correlation {}", valueSpecification, correlationId);
    
    long specificationId = getValueSpecificationId(valueSpecification);
    
    ValueLookupRequest request = new ValueLookupRequest();
    request.setCorrelationId(correlationId);
    request.setViewName(viewName);
    request.setCalculationConfigurationName(calcConfigName);
    request.setSnapshot(timestamp);
    request.setSpecificationId(specificationId);
    
    FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    MutableFudgeFieldContainer requestMsg = ctx.objectToFudgeMsg(request);
    FudgeSerializationContext.addClassHeader (requestMsg, ValueLookupRequest.class);
    
    Object resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof ValueLookupResponse;
    ValueLookupResponse lookupResponse = (ValueLookupResponse) resultValue;
    return lookupResponse.getValue();
  }
  
  public void putValue(String viewName, String calcConfigName, long timestamp, ComputedValue value) {
    long correlationId = getNextCorrelationId();
    s_logger.info("Submitting value {} - Correlation {}", value.getSpecification(), correlationId);
    
    long specificationId = getValueSpecificationId(value.getSpecification());
    
    ValuePutRequest request = new ValuePutRequest();
    request.setCorrelationId(correlationId);
    request.setViewName(viewName);
    request.setCalculationConfigurationName(calcConfigName);
    request.setSnapshot(timestamp);
    request.setSpecificationId(specificationId);
    request.setValue(value.getValue ());

    FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    MutableFudgeFieldContainer requestMsg = ctx.objectToFudgeMsg(request);
    FudgeSerializationContext.addClassHeader (requestMsg, ValuePutRequest.class);
    
    Object resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof ValuePutResponse;
    // Nothing to check.
  }
  
  public int purgeCache(String viewName, String calcConfigName, long timestamp) {
    long correlationId = getNextCorrelationId();
    s_logger.info("Submitting Purge {} {} {} - Correlation {}",
        new Object[] {viewName, calcConfigName, timestamp, correlationId} );
    
    CachePurgeRequest request = new CachePurgeRequest();
    request.setCorrelationId(correlationId);
    request.setViewName(viewName);
    if(calcConfigName != null) {
      request.setCalculationConfigurationName(calcConfigName);
    }
    request.setSnapshot(timestamp);

    FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    MutableFudgeFieldContainer requestMsg = ctx.objectToFudgeMsg(request);
    FudgeSerializationContext.addClassHeader (requestMsg, CachePurgeRequest.class);
    
    Object resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof CachePurgeResponse;
    CachePurgeResponse purgeResponse = (CachePurgeResponse) resultValue;
    s_logger.info("Purge {} {} {} purged {} caches - Correlation {}",
        new Object[] {viewName, calcConfigName, timestamp, purgeResponse.getNumPurged(), correlationId} );
    return purgeResponse.getNumPurged();
  }

  @Override
  protected long getCorrelationIdFromReply(Object reply) {
    if(!(reply instanceof RemoteCacheMessage)) {
      s_logger.error("Didn't get a RemoteCacheMessage reply, got a {}", reply);
      return Long.MIN_VALUE;
    }
    RemoteCacheMessage remoteCacheMessage = (RemoteCacheMessage) reply;
    return remoteCacheMessage.getCorrelationId();
  }
}
