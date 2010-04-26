/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Allows synchronous RPC-style semantics to be applied over a
 * {@link FudgeRequestSender}.
 * This class also supports multiplexing different clients over the same
 * underlying transport channel using correlation IDs to multiplex the requests
 * and responses. 
 *
 * @author kirk
 */
public abstract class FudgeSynchronousClient implements FudgeMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(FudgeSynchronousClient.class);
  public static final long DEFAULT_TIMEOUT_IN_MILLISECONDS = 30 * 1000l;
  private final AtomicLong _nextCorrelationId = new AtomicLong(1l);
  private final FudgeRequestSender _requestSender;
  private final Map<Long, ClientRequestHolder> _pendingRequests = new ConcurrentHashMap<Long, ClientRequestHolder>();
  private final long _timeoutInMilliseconds;
  
  protected FudgeSynchronousClient(FudgeRequestSender requestSender) {
    this(requestSender, DEFAULT_TIMEOUT_IN_MILLISECONDS);
  }
  
  protected FudgeSynchronousClient(FudgeRequestSender requestSender, long timeoutInMilliseconds) {
    ArgumentChecker.notNull(requestSender, "Fudge request sender");
    _requestSender = requestSender;
    _timeoutInMilliseconds = timeoutInMilliseconds;
  }

  /**
   * @return the requestSender
   */
  public FudgeRequestSender getRequestSender() {
    return _requestSender;
  }

  /**
   * @return the timeoutInMilliseconds
   */
  public long getTimeoutInMilliseconds() {
    return _timeoutInMilliseconds;
  }
  
  protected long getNextCorrelationId() {
    return _nextCorrelationId.getAndIncrement();
  }

  protected Object sendRequestAndWaitForResponse(FudgeFieldContainer requestMsg, long correlationId) {
    ClientRequestHolder requestHolder = new ClientRequestHolder();
    _pendingRequests.put(correlationId, requestHolder);
    getRequestSender().sendRequest(requestMsg, this);
    try {
      requestHolder.latch.await(getTimeoutInMilliseconds(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Didn't get response to {} in {}ms", correlationId, getTimeoutInMilliseconds());
      // Always remove it to clear out the Map so that in a long-running system we don't
      // end up with massive memory leaks.
      _pendingRequests.remove(correlationId);
    }
    if(requestHolder.resultValue == null) {
      throw new OpenGammaRuntimeException("Didn't receive a response message to " + correlationId + " in " + getTimeoutInMilliseconds() + "ms");
    }
    assert getCorrelationIdFromReply(requestHolder.resultValue) == correlationId;
    return requestHolder.resultValue;
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext,
      FudgeMsgEnvelope msgEnvelope) {
    Object reply = fudgeContext.fromFudgeMsg(msgEnvelope.getMessage());
    long correlationId = getCorrelationIdFromReply(reply);
    ClientRequestHolder requestHolder = _pendingRequests.remove(correlationId);
    if(requestHolder == null) {
      s_logger.warn("Got a response on correlation Id {} which didn't match a pending request.", correlationId);
      return;
    }
    requestHolder.resultValue = reply;
    requestHolder.latch.countDown();
  }
  
  protected abstract long getCorrelationIdFromReply(Object reply);
  
  private static final class ClientRequestHolder {
    public Object resultValue;
    public final CountDownLatch latch = new CountDownLatch(1); 
  }

}
