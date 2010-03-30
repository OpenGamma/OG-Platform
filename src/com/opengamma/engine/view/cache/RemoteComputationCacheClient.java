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
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class RemoteComputationCacheClient implements FudgeMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteComputationCacheClient.class);
  private final FudgeRequestSender _requestSender;
  private final AtomicLong _nextCorrelationId = new AtomicLong(1l);
  private final Map<Long, ClientRequestHolder> _pendingRequests = new ConcurrentHashMap<Long, ClientRequestHolder>();
  private final ConcurrentMap<ValueSpecification, Long> _specificationIds = new ConcurrentHashMap<ValueSpecification, Long>();
  
  public RemoteComputationCacheClient(FudgeRequestSender requestSender) {
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
    FudgeFieldContainer requestMsg = request.toFudgeMsg(new FudgeSerializationContext(getRequestSender().getFudgeContext()));
    ClientRequestHolder requestHolder = new ClientRequestHolder();
    _pendingRequests.put(correlationId, requestHolder);
    getRequestSender().sendRequest(requestMsg, this);
    try {
      requestHolder.latch.await();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    if(requestHolder.resultValue == null) {
      throw new OpenGammaRuntimeException("Didn't receive a response message to get value specification ID for " + valueSpec);
    }
    assert requestHolder.resultValue.getCorrelationId() == correlationId;
    assert requestHolder.resultValue instanceof ValueSpecificationLookupResponse;
    ValueSpecificationLookupResponse lookupResponse = (ValueSpecificationLookupResponse) requestHolder.resultValue;
    return lookupResponse.getResponse();
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

  private static final class ClientRequestHolder {
    public RemoteCacheMessage resultValue;
    public final CountDownLatch latch = new CountDownLatch(1); 
  }

}
