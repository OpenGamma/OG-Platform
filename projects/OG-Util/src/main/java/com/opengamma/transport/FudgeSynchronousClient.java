/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Allows synchronous RPC-style semantics to be applied over a {@link FudgeRequestSender}
 * or {@link FudgeConnection}. This class also supports multiplexing different clients over
 * the same underlying transport channel using correlation IDs to multiplex the requests
 * and responses.
 */
public abstract class FudgeSynchronousClient implements FudgeMessageReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(FudgeSynchronousClient.class);
  /**
   * The default timeout.
   */
  private static final long DEFAULT_TIMEOUT_IN_MILLISECONDS = 30 * 1000L;

  /**
   * The generator of correlation ids.
   */
  private final AtomicLong _nextCorrelationId = new AtomicLong();
  /**
   * The Fudge message sender.
   */
  private final FudgeMessageSender _messageSender;
  /**
   * The map of pending requests keyed by correlation id.
   */
  private final Map<Long, ClientRequestHolder> _pendingRequests = new ConcurrentHashMap<Long, ClientRequestHolder>();
  /**
   * The timeout.
   */
  private long _timeoutInMilliseconds = DEFAULT_TIMEOUT_IN_MILLISECONDS;
  /**
   * Handler for asynchronous messages.
   */
  private FudgeMessageReceiver _asynchronousMessageReceiver;

  /**
   * Creates the client.
   * @param requestSender  the sender, not null
   */
  protected FudgeSynchronousClient(final FudgeRequestSender requestSender) {
    ArgumentChecker.notNull(requestSender, "requestSender");
    _messageSender = new FudgeMessageSender() {

      @Override
      public FudgeContext getFudgeContext() {
        return requestSender.getFudgeContext();
      }

      @Override
      public void send(FudgeMsg message) {
        requestSender.sendRequest(message, FudgeSynchronousClient.this);
      }

    };
  }

  protected FudgeSynchronousClient(final FudgeConnection connection) {
    ArgumentChecker.notNull(connection, "connection");
    connection.setFudgeMessageReceiver(this);
    _messageSender = connection.getFudgeMessageSender();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the message sender.
   * 
   * @return the message sender, not null
   */
  public FudgeMessageSender getMessageSender() {
    return _messageSender;
  }

  /**
   * Gets the timeout in milliseconds.
   * 
   * @return the timeout
   */
  public long getTimeoutInMilliseconds() {
    return _timeoutInMilliseconds;
  }

  public void setTimeoutInMilliseconds(final long timeoutMilliseconds) {
    _timeoutInMilliseconds = timeoutMilliseconds;
  }

  public void setAsynchronousMessageReceiver(final FudgeMessageReceiver asynchronousMessageReceiver) {
    _asynchronousMessageReceiver = asynchronousMessageReceiver;
  }

  public FudgeMessageReceiver getAsynchronousMessageReceiver() {
    return _asynchronousMessageReceiver;
  }

  /**
   * Gets the next id.
   * 
   * @return the next numeric id
   */
  protected long getNextCorrelationId() {
    return _nextCorrelationId.incrementAndGet();
  }

  //-------------------------------------------------------------------------
  /**
   * Sends the message.
   * 
   * @param requestMsg  the message, not null
   * @param correlationId  the message id
   * @return the result
   */
  protected FudgeMsg sendRequestAndWaitForResponse(FudgeMsg requestMsg, long correlationId) {
    ClientRequestHolder requestHolder = new ClientRequestHolder();
    _pendingRequests.put(correlationId, requestHolder);
    try {
      s_logger.debug("Sending message {}", correlationId);
      getMessageSender().send(requestMsg);
      try {
        s_logger.debug("Blocking for message result");
        requestHolder.latch.await(getTimeoutInMilliseconds(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.error("Interrupted");
      }
      if (requestHolder.resultValue == null) {
        s_logger.warn("Didn't get response to {} in {}ms", correlationId, getTimeoutInMilliseconds());
        throw new OpenGammaRuntimeException("Didn't receive a response message to " + correlationId + " in " + getTimeoutInMilliseconds() + "ms");
      }
      assert getCorrelationIdFromReply(requestHolder.resultValue) == correlationId;
      s_logger.debug("Received result {}", requestHolder.resultValue);
      return requestHolder.resultValue;
    } finally {
      _pendingRequests.remove(correlationId);
      s_logger.debug("Request {} complete", correlationId);
    }
  }

  protected void sendMessage(FudgeMsg message) {
    getMessageSender().send(message);
  }

  /**
   * Receives a message from Fudge.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param msgEnvelope  the message, not null
   */
  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    final FudgeMsg reply = msgEnvelope.getMessage();
    final Long correlationId = getCorrelationIdFromReply(reply);
    if (correlationId == null) {
      final FudgeMessageReceiver receiver = getAsynchronousMessageReceiver();
      if (receiver == null) {
        s_logger.info("Unhandled asynchronous message {}", msgEnvelope);
      } else {
        receiver.messageReceived(fudgeContext, msgEnvelope);
      }
      return;
    }
    final ClientRequestHolder requestHolder = _pendingRequests.remove(correlationId);
    if (requestHolder == null) {
      s_logger.warn("Got a response on non-pending correlation Id {}", correlationId);
      return;
    }
    requestHolder.resultValue = reply;
    requestHolder.latch.countDown();
  }

  /**
   * Extracts the correlation id from the reply object.
   * 
   * @param reply  the reply
   * @return the id, null if it's an asynchronous message (over {@link FudgeConnection} transport only)
   */
  protected abstract Long getCorrelationIdFromReply(FudgeMsg reply);

  //-------------------------------------------------------------------------
  /**
   * Data holder.
   */
  private static final class ClientRequestHolder {
    public FudgeMsg resultValue; // CSIGNORE: simple holder object
    public final CountDownLatch latch = new CountDownLatch(1); // CSIGNORE: simple holder object
  }

}
