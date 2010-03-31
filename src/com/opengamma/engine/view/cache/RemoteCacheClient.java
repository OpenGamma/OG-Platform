/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class RemoteCacheClient implements FudgeMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheClient.class);
  private final FudgeRequestSender _requestSender;
  private final AtomicLong _nextCorrelationId = new AtomicLong(1l);
  private final Map<Long, ClientRequestHolder> _pendingRequests = new ConcurrentHashMap<Long, ClientRequestHolder>();
  private final ConcurrentMap<ValueSpecification, Long> _specificationIds = new ConcurrentHashMap<ValueSpecification, Long>();
  
  public RemoteCacheClient(FudgeRequestSender requestSender) {
    ArgumentChecker.checkNotNull(requestSender, "Fudge request sender");
    _requestSender = requestSender;
  }

  /**
   * @return the requestSender
   */
  public FudgeRequestSender getRequestSender() {
    return _requestSender;
  }
  
  public long getValueSpecificationId(ValueSpecification valueSpec) {
    ArgumentChecker.checkNotNull(valueSpec, "Value Specification");
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
    ArgumentChecker.checkNotNull(valueSpec, "Value Specification");
    long correlationId = _nextCorrelationId.getAndIncrement();
    s_logger.info("Requesting value specification ID for {} - Correlation {}", valueSpec, correlationId);
    ValueSpecificationLookupRequest request = new ValueSpecificationLookupRequest(correlationId, valueSpec);
    
    FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    MutableFudgeFieldContainer requestMsg = ctx.objectToFudgeMsg(request);
    FudgeSerializationContext.addClassHeader (requestMsg, ValueSpecificationLookupRequest.class);
    
    //FudgeFieldContainer requestMsg = request.toFudgeMsg(new FudgeSerializationContext(getRequestSender().getFudgeContext()));
    RemoteCacheMessage resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof ValueSpecificationLookupResponse;
    ValueSpecificationLookupResponse lookupResponse = (ValueSpecificationLookupResponse) resultValue;
    return lookupResponse.getResponse();
  }
  
  public ComputedValue getValue(String viewName, String calcConfigName, long timestamp, ValueSpecification valueSpecification) {
    // TODO kirk 2010-03-30 -- Check Inputs.
    
    long correlationId = _nextCorrelationId.getAndIncrement();
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
    
    RemoteCacheMessage resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof ValueLookupResponse;
    ValueLookupResponse lookupResponse = (ValueLookupResponse) resultValue;
    return lookupResponse.getValue();
  }
  
  public void putValue(String viewName, String calcConfigName, long timestamp, ComputedValue value) {
    long correlationId = _nextCorrelationId.getAndIncrement();
    s_logger.info("Submitting value {} - Correlation {}", value.getSpecification(), correlationId);
    
    long specificationId = getValueSpecificationId(value.getSpecification());
    
    ValuePutRequest request = new ValuePutRequest();
    request.setCorrelationId(correlationId);
    request.setViewName(viewName);
    request.setCalculationConfigurationName(calcConfigName);
    request.setSnapshot(timestamp);
    request.setSpecificationId(specificationId);
    request.setValue(value);

    FudgeSerializationContext ctx = new FudgeSerializationContext(getRequestSender().getFudgeContext());
    MutableFudgeFieldContainer requestMsg = ctx.objectToFudgeMsg(request);
    FudgeSerializationContext.addClassHeader (requestMsg, ValuePutRequest.class);
    
    RemoteCacheMessage resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof ValuePutResponse;
    // Nothing to check.
  }
  
  public int purgeCache(String viewName, String calcConfigName, long timestamp) {
    long correlationId = _nextCorrelationId.getAndIncrement();
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
    
    RemoteCacheMessage resultValue = sendRequestAndWaitForResponse(requestMsg, correlationId);
    assert resultValue instanceof CachePurgeResponse;
    CachePurgeResponse purgeResponse = (CachePurgeResponse) resultValue;
    s_logger.info("Purge {} {} {} purged {} caches - Correlation {}",
        new Object[] {viewName, calcConfigName, timestamp, purgeResponse.getNumPurged(), correlationId} );
    return purgeResponse.getNumPurged();
  }
  
  @Override
  public void messageReceived(FudgeContext fudgeContext,
      FudgeMsgEnvelope msgEnvelope) {
    Object reply = fudgeContext.fromFudgeMsg(msgEnvelope.getMessage());
    if(!(reply instanceof RemoteCacheMessage)) {
      s_logger.warn("Didn't get a RemoteCacheMessage from our response channel, got {} instead", reply);
      return;
    }
    RemoteCacheMessage remoteCacheMessage = (RemoteCacheMessage) reply;
    long correlationId = remoteCacheMessage.getCorrelationId();
    ClientRequestHolder requestHolder = _pendingRequests.remove(correlationId);
    if(requestHolder == null) {
      s_logger.warn("Got a response on correlation Id {} which didn't match a pending request.", correlationId);
      return;
    }
    requestHolder.resultValue = remoteCacheMessage;
    requestHolder.latch.countDown();
  }
  
  protected RemoteCacheMessage sendRequestAndWaitForResponse(FudgeFieldContainer requestMsg, long correlationId) {
    ClientRequestHolder requestHolder = new ClientRequestHolder();
    _pendingRequests.put(correlationId, requestHolder);
    getRequestSender().sendRequest(requestMsg, this);
    try {
      requestHolder.latch.await();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    if(requestHolder.resultValue == null) {
      throw new OpenGammaRuntimeException("Didn't receive a response message - " + correlationId);
    }
    assert requestHolder.resultValue.getCorrelationId() == correlationId;
    return requestHolder.resultValue;
  }

  private static final class ClientRequestHolder {
    public RemoteCacheMessage resultValue;
    public final CountDownLatch latch = new CountDownLatch(1); 
  }

}
