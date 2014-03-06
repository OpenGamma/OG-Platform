/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.transport.jms.JmsTemporaryQueueHost;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Base class for a remote consumer which uses a REST+JMS pattern to access streaming results.
 * <p>
 * Provides heartbeating and control of the JMS stream.
 *
 * @param <L> the type of the listener which will receive the results from the consumer.
 */
public abstract class AbstractRestfulJmsResultConsumer<L> {

  private static final long START_JMS_RESULT_STREAM_TIMEOUT_MILLIS = 10000;
  private static final int HEARTBEAT_RETRIES = 3;

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractRestfulJmsResultConsumer.class);

  /**
   * The base URI
   */
  private final URI _baseUri;
  /**
   * The REST client
   */
  private final FudgeRestClient _client;
  /**
   * The heartbeat
   */
  private final ScheduledFuture<?> _scheduledHeartbeat;
  /**
   * The JMS connector
   */
  private final JmsConnector _jmsConnector;
  /**
   * The Fudge context
   */
  private final FudgeContext _fudgeContext;
  /**
   * The demand of listeners
   */
  private long _listenerDemand;
  /**
   * The temporary queue host
   */
  private JmsTemporaryQueueHost _queueHost;
  /**
   * The started signal latch
   */
  private CountDownLatch _startedSignalLatch;

  protected AbstractRestfulJmsResultConsumer(URI baseUri, FudgeContext fudgeContext, JmsConnector jmsConnector, ScheduledExecutorService scheduler, long heartbeatPeriodMillis) {
    _baseUri = baseUri;
    _jmsConnector = jmsConnector;
    _fudgeContext = fudgeContext;
    _client = FudgeRestClient.create();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        heartbeat();
      }
    };
    _scheduledHeartbeat = scheduler.scheduleAtFixedRate(runnable, heartbeatPeriodMillis, heartbeatPeriodMillis, TimeUnit.MILLISECONDS);
  }

  //-------------------------------------------------------------------------
  protected void onStartResultStream() {
  }

  protected void onEndResultStream() {
  }

  protected abstract void dispatchListenerCall(Function<L, ?> listenerCall);

  //-------------------------------------------------------------------------
  protected URI getBaseUri() {
    return _baseUri;
  }

  protected FudgeRestClient getClient() {
    return _client;
  }

  //-------------------------------------------------------------------------
  /**
   * Externally visible for testing.
   */
  public void heartbeat() {
    URI heartbeatUri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_HEARTBEAT);
    heartbeat(heartbeatUri);
  }

  /**
   * Externally visible for testing.
   * 
   * @param heartbeatUri the heartbeat URI, not null
   */
  public void heartbeat(URI heartbeatUri) {
    ArgumentChecker.notNull(heartbeatUri, "heartbeatUri");
    for (int i = 1; i <= HEARTBEAT_RETRIES; i++) {
      try {
        _client.accessFudge(heartbeatUri).post();
        return;
      } catch (Exception ex) {
        if (s_logger.isDebugEnabled()) {
          s_logger.debug("Heartbeat attempt " + i + " of " + HEARTBEAT_RETRIES + " failed", ex);
        } else {
          s_logger.warn("Heartbeat attempt " + i + " of " + HEARTBEAT_RETRIES + " failed" + ex.toString());
        }
        if (i == HEARTBEAT_RETRIES) {
          heartbeatFailed(ex);
        }
      }
    }
  }

  /**
   * Called when heartbeating has failed, indicating that the remote resource has been discarded or the connection to the remote host has been lost. This is intended to be overridden to add custom
   * error handling.
   * <p>
   * Externally visible for testing.
   * 
   * @param ex an exception associated with the failed heartbeat, may be null
   */
  public void heartbeatFailed(Exception ex) {
    s_logger.error("Heartbeating failed for resource " + getBaseUri() + " failed", ex);
  }

  /**
   * Externally visible for testing
   */
  public void stopHeartbeating() {
    if (!_scheduledHeartbeat.isCancelled()) {
      _scheduledHeartbeat.cancel(true);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Increments the listener demand, starting the underlying subscription if this is the first listener.
   * <p>
   * If an exception is thrown then the listener state remains unchanged.
   * 
   * @throws InterruptedException if the thread is interrupted while starting the subscription
   * @throws JMSException if a JMS error occurs while starting the subscription
   */
  protected void incrementListenerDemand() throws InterruptedException, JMSException {
    _listenerDemand++;
    try {
      configureResultListener();
    } catch (JMSException | InterruptedException e) {
      _listenerDemand--;
      throw e;
    }
  }

  /**
   * Decrements the listener demand, stopping the underlying subscription if the last listener is removed.
   * <p>
   * If an exception is thrown then the listener state remains unchanged.
   * 
   * @throws InterruptedException if the thread is interrupted while stopping the subscription
   * @throws JMSException if a JMS error occurs while stopping the subscription
   */
  protected void decrementListenerDemand() throws InterruptedException, JMSException {
    _listenerDemand--;
    try {
      configureResultListener();
    } catch (JMSException | InterruptedException e) {
      _listenerDemand--;
      throw e;
    }
  }

  /**
   * Configures the underlying subscription if required.
   * <p>
   * If an exception is thrown then the listener state remains unchanged.
   * 
   * @throws InterruptedException if the thread is interrupted while configuring the subscription
   * @throws JMSException if a JMS error occurs while configuring the subscription
   */
  private void configureResultListener() throws InterruptedException, JMSException {
    if (_listenerDemand == 0) {
      URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_STOP_JMS_RESULT_STREAM);
      getClient().accessFudge(uri).post();
      closeJms();
      onEndResultStream();
    } else if (_listenerDemand == 1) {
      String destination = startJms();
      MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msg.add(AbstractRestfulJmsResultPublisher.DESTINATION_FIELD, destination);
      URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_START_JMS_RESULT_STREAM);
      getClient().accessFudge(uri).post(msg);
      try {
        if (!_startedSignalLatch.await(START_JMS_RESULT_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
          s_logger.error("Timed out after {} ms waiting for JMS result stream to be started", START_JMS_RESULT_STREAM_TIMEOUT_MILLIS);
          closeJms();
          throw new OpenGammaRuntimeException("Timed out after " + START_JMS_RESULT_STREAM_TIMEOUT_MILLIS + " ms waiting for JMS result stream to be started");
        }
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted while starting JMS result stream");
        closeJms();
        throw e;
      }
      onStartResultStream();
    }
  }

  private String startJms() throws JMSException {
    try {
      _startedSignalLatch = new CountDownLatch(1);
      ByteArrayFudgeMessageReceiver bafmr = new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
          s_logger.debug("Result listener call received");
          Function<L, ?> listenerCall;
          try {
            if (msgEnvelope.getMessage().getNumFields() == 0) {
              // Empty message = started signal, should never occur at other times
              s_logger.debug("Received started signal");
              _startedSignalLatch.countDown();
              return;
            }
            listenerCall = fudgeContext.fromFudgeMsg(Function.class, msgEnvelope.getMessage());
          } catch (Throwable t) {
            s_logger.error("Couldn't parse message {}", t.getMessage());
            s_logger.warn("Caught exception parsing message", t);
            s_logger.debug("Couldn't parse message {}", msgEnvelope.getMessage());
            return;
          }
          try {
            dispatchListenerCall(listenerCall);
          } catch (Throwable t) {
            s_logger.error("Error dispatching " + listenerCall + " to listener", t);
          }
        }
      }, _fudgeContext);
      _queueHost = new JmsTemporaryQueueHost(_jmsConnector, new JmsByteArrayMessageDispatcher(bafmr));

      s_logger.info("Set up result JMS subscription to {}", _queueHost.getQueueName());
      return _queueHost.getQueueName();
    } catch (JMSException e) {
      s_logger.error("Exception setting up JMS result listener", e);
      closeJms();
      throw e;
    }
  }

  private void closeJms() {
    if (_queueHost != null) {
      try {
        _queueHost.close();
        _startedSignalLatch = null;
      } catch (Exception e) {
        s_logger.error("Error closing JMS queue host", e);
      }
    }
  }

  //-------------------------------------------------------------------------
  protected static URI getUri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).path(path).build();
  }

}
