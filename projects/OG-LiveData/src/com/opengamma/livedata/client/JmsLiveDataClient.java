/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.jms.support.JmsUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * A JMS LiveData client. This client is implemented using JMS's asynchronous
 * onMessage() notification capability.
 * The client creates 10 JMS sessions by default. New market data
 * subscriptions are assigned to sessions in round-robin fashion.
 */
@PublicAPI
public class JmsLiveDataClient extends DistributedLiveDataClient implements Lifecycle {
  
  /**
   * How many JMS sessions the client will create by default
   */
  public static final int DEFAULT_NUM_SESSIONS = 10;
  
  private static final Logger s_logger = LoggerFactory.getLogger(JmsLiveDataClient.class);
  
  private final JmsConnector _jmsConnector;
  private volatile Connection _connection;
  
  private final Map<String, MessageConsumer> _messageConsumersBySpec =
    new HashMap<String, MessageConsumer>();
  
  /**
   * A list of JMS sessions created so far. The size of the list
   * will not exceed _maxSessions.
   */
  private final List<Session> _sessions = new ArrayList<Session>();
  
  /**
   * How many JMS sessions the client will create. Must be positive. 
   */
  private final int _maxSessions;
  
  /**
   * This needs to be in [0, _maxSessions - 1] at all times.
   * When it reaches _maxSessions, it will be reset back to 0.
   */
  private int _currentSessionIndex; // = 0
  
  private AtomicBoolean _running = new AtomicBoolean(false);
  
  private ExecutorService _executor;
  
  public JmsLiveDataClient(FudgeRequestSender subscriptionRequestSender,
      FudgeRequestSender entitlementRequestSender,
      JmsConnector jmsConnector) {
    this(subscriptionRequestSender, 
        entitlementRequestSender, 
        jmsConnector, 
        OpenGammaFudgeContext.getInstance(),
        DEFAULT_NUM_SESSIONS);
  }

  public JmsLiveDataClient(FudgeRequestSender subscriptionRequestSender,
      FudgeRequestSender entitlementRequestSender,
      JmsConnector jmsConnector, 
      FudgeContext fudgeContext,
      int maxSessions) {
    super(subscriptionRequestSender, entitlementRequestSender, fudgeContext);
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    _jmsConnector = jmsConnector;
    
    if (maxSessions <= 0) {
      throw new IllegalArgumentException("Max sessions must be positive");
    }
    _maxSessions = maxSessions;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * 
   * @return the JMS connector
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  @Override
  public synchronized void startReceivingTicks(Collection<String> tickDistributionSpecifications) {
    super.startReceivingTicks(tickDistributionSpecifications);
    
    s_logger.info("Starting listening to tick distribution specifications {}", tickDistributionSpecifications);
    
    List<List<String>> specsBySessionIndex = new ArrayList<List<String>>(_maxSessions);
    
    for (String tickDistributionSpecification : tickDistributionSpecifications) {
      if (_messageConsumersBySpec.containsKey(tickDistributionSpecification)) {
        // Already receiving for that tick. Ignore it.
        continue;
      }

      while (specsBySessionIndex.size() <= _currentSessionIndex) {
        specsBySessionIndex.add(new ArrayList<String>());
      }
      
      try {
        Session session;
        if (_sessions.size() <= _currentSessionIndex) {
          session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          _sessions.add(session);
        } else {
          session = _sessions.get(_currentSessionIndex);
        }        
      } catch (JMSException e) {
        throw new OpenGammaRuntimeException("Failed to create subscription to JMS topic " + tickDistributionSpecification, e);      
      }

      specsBySessionIndex.get(_currentSessionIndex).add(tickDistributionSpecification);

      // round-robin logic here
      _currentSessionIndex++;
      if (_currentSessionIndex >= _maxSessions) {
        _currentSessionIndex = 0;
      }
    }
    List<Future<Map<String, MessageConsumer>>> futures = new ArrayList<Future<Map<String, MessageConsumer>>>(); 
    for (int i = 0; i < specsBySessionIndex.size(); i++) {
      Callable<Map<String, MessageConsumer>> c = getStartReceivingCallable(specsBySessionIndex.get(i), i);
      futures.add(_executor.submit(c));
    }
    for (Future<Map<String, MessageConsumer>> future : futures) {
      try {
        Map<String, MessageConsumer> consumers = future.get();
        _messageConsumersBySpec.putAll(consumers);
      } catch (ExecutionException ex) {
        throw new OpenGammaRuntimeException("Failed to start receiving ticks", ex);
      } catch (InterruptedException ex) {
        throw new OpenGammaRuntimeException("Failed to start receiving ticks", ex);
      }
    }
  }

  private Callable<Map<String, MessageConsumer>> getStartReceivingCallable(final List<String> specs, final int sessionIndex) {
    return new Callable<Map<String, MessageConsumer>>() {
      @Override
      public Map<String, MessageConsumer> call() {
        ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(JmsLiveDataClient.this, getFudgeContext());
        final JmsByteArrayMessageDispatcher jmsDispatcher = new JmsByteArrayMessageDispatcher(fudgeReceiver);
        Session session = _sessions.get(sessionIndex);
        
        Map<String, MessageConsumer> ret = new HashMap<String, MessageConsumer>();
        for (String tickDistributionSpecification : specs) {
          try {
            Topic topic = session.createTopic(tickDistributionSpecification);

            MessageConsumer messageConsumer = session.createConsumer(topic);
            messageConsumer.setMessageListener(jmsDispatcher);
            ret.put(tickDistributionSpecification, messageConsumer);
          } catch (JMSException e) {
            throw new OpenGammaRuntimeException("Failed to create subscription to JMS topic "
                + tickDistributionSpecification, e);
          }
        }
        return ret;
      }
    };
  }
  
  @Override
  public synchronized void stopReceivingTicks(String tickDistributionSpecification) {
    MessageConsumer consumer = _messageConsumersBySpec.get(tickDistributionSpecification);
    if (consumer == null) {
      return;
    }
    
    try {
      consumer.close();
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to close subscription to JMS topic " + tickDistributionSpecification, e);
    }
    
    _messageConsumersBySpec.remove(tickDistributionSpecification);
  }

  @Override
  public boolean isRunning() {
    return _running.get();
  }

  @Override
  public synchronized void start() {
    try {
      _connection = _jmsConnector.getConnectionFactory().createConnection();
      _connection.start();
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create JMS connection", e);
    }
    _executor = Executors.newCachedThreadPool();
    _running.set(true);
  }

  @Override
  public synchronized void close() {
    try {
      for (Session session : _sessions) {
        s_logger.info("Shutting down session {}", session);
        session.close();
      }
      _sessions.clear();
      for (MessageConsumer consumer : _messageConsumersBySpec.values()) {
        JmsUtils.closeMessageConsumer(consumer); // [PLAT-1809]  Must close these as well
      }
      _messageConsumersBySpec.clear();
      if (_connection != null) {
        _connection.close();
        _connection = null;
      }
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to close JMS connection", e);
    }
    _executor.shutdown();
    super.close();
  }

  @Override
  public synchronized void stop() {
    close();
    _running.set(false);
  }

}
