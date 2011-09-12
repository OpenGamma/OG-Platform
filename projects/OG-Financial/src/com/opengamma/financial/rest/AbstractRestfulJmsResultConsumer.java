/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Base class for a remote consumer which uses a REST+JMS pattern to access streaming results.
 * <p>
 * Provides heartbeating and control of the JMS stream.
 */
public abstract class AbstractRestfulJmsResultConsumer {

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
   * The heartbeat.
   */
  private final ScheduledFuture<?> _scheduledHeartbeat;
  /**
   * The connection factory.
   */
  private final ConnectionFactory _connectionFactory;
  /**
   * The Fudge context
   */
  private final FudgeContext _fudgeContext;
  /**
   * The demand of listeners.
   */
  private long _listenerDemand;
  /**
   * The message consumer.
   */
  private MessageConsumer _consumer;
  /**
   * The connection.
   */
  private Connection _connection;
  
  protected AbstractRestfulJmsResultConsumer(URI baseUri, FudgeContext fudgeContext, ConnectionFactory connectionFactory, ScheduledExecutorService scheduler, long heartbeatPeriodMillis) {
    _baseUri = baseUri;
    _connectionFactory = connectionFactory;
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
  
  protected abstract void dispatchListenerCall(Function<?, ?> listenerCall);
  
  //-------------------------------------------------------------------------
  protected URI getBaseUri() {
    return _baseUri;
  }
  
  protected FudgeRestClient getClient() {
    return _client;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Externally visible for testing
   */
  public void heartbeat() {
    URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_HEARTBEAT);
    _client.access(uri).post();
  }

  /**
   * Externally visible for testing
   */
  public void stopHeartbeating() {
    _scheduledHeartbeat.cancel(true);
  }
  
  //-------------------------------------------------------------------------
  protected void incrementListenerDemand() throws JMSException {
    _listenerDemand++;
    configureResultListener();
  }
  
  protected void decrementListenerDemand() throws JMSException {
    _listenerDemand--;
    configureResultListener();
  }
  
  private void configureResultListener() throws JMSException {
    if (_listenerDemand == 0) {
      URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_STOP_JMS_RESULT_STREAM);
      getClient().access(uri).post();
      closeJms();
    } else if (_listenerDemand == 1) {
      String destination = startJms();
      MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msg.add(AbstractRestfulJmsResultPublisher.DESTINATION_FIELD, destination);
      URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_START_JMS_RESULT_STREAM);
      getClient().access(uri).post(msg);
    }
  }
  
  private String startJms() throws JMSException {
    try {
      _connection = _connectionFactory.createConnection();
      Session session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TemporaryQueue tempQueue = session.createTemporaryQueue();
      _consumer = session.createConsumer(tempQueue);
      _consumer.setMessageListener(new JmsByteArrayMessageDispatcher(new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
          s_logger.debug("Result listener call received");
          Function<ViewResultListener, ?> listenerCall;
          try {
            listenerCall = fudgeContext.fromFudgeMsg(Function.class, msgEnvelope.getMessage());
          } catch (Exception e) {
            s_logger.warn("Caught exception parsing message", e);
            s_logger.debug("Couldn't parse message {}", msgEnvelope.getMessage());
            return;
          }
          dispatchListenerCall(listenerCall);
        }
      }, _fudgeContext)));
      _connection.start();
      s_logger.info("Set up result JMS subscription to {}", tempQueue);
      return tempQueue.getQueueName();
    } catch (JMSException e) {
      s_logger.error("Exception setting up JMS result listener", e);
      closeJms();
      throw e;
    }
  }
  
  private void closeJms() {
    if (_consumer != null) {
      try {
        _connection.close();
      } catch (Exception e) {
        s_logger.error("Error closing JMS connection", e);
      } finally {
        _connection = null;
        _consumer = null;
      }
    }
  }
  
  //-------------------------------------------------------------------------
  protected static URI getUri(URI baseUri, String path) {
    return UriBuilder.fromUri(baseUri).path(path).build();
  }
  
}
