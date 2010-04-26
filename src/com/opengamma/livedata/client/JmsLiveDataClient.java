/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.ConnectionFactory;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class JmsLiveDataClient extends DistributedLiveDataClient implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsLiveDataClient.class);
  private final ConnectionFactory _connectionFactory;
  private final Map<String, DefaultMessageListenerContainer> _listenerContainersBySpec =
    new HashMap<String, DefaultMessageListenerContainer>();
  private AtomicBoolean _running = new AtomicBoolean(false);

  public JmsLiveDataClient(FudgeRequestSender subscriptionRequestSender, ConnectionFactory connectionFactory) {
    this(subscriptionRequestSender, connectionFactory, new FudgeContext());
  }

  public JmsLiveDataClient(FudgeRequestSender subscriptionRequestSender, ConnectionFactory connectionFactory, FudgeContext fudgeContext) {
    super(subscriptionRequestSender, fudgeContext);
    ArgumentChecker.notNull(connectionFactory, "JMS Connection Factory");
    _connectionFactory = connectionFactory;
  }
  
  /**
   * @return the connectionFactory
   */
  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  // TODO kirk 2009-10-30 -- Tear down unnecessary distribution specifications when we don't
  // need them anymore.

  @Override
  public synchronized void startReceivingTicks(String tickDistributionSpecification) {
    super.startReceivingTicks(tickDistributionSpecification);
    
    if(_listenerContainersBySpec.containsKey(tickDistributionSpecification)) {
      // Already receiving for that tick. Ignore it.
      return;
    }
    
    s_logger.info("Starting listening to tick distribution specification {}", tickDistributionSpecification);
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(this);
    JmsByteArrayMessageDispatcher jmsDispatcher = new JmsByteArrayMessageDispatcher(fudgeReceiver);
    
    DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
    listenerContainer.setDestinationName(tickDistributionSpecification);
    listenerContainer.setPubSubDomain(true);
    listenerContainer.setConnectionFactory(getConnectionFactory());
    listenerContainer.setMaxConcurrentConsumers(1);
    listenerContainer.setMessageListener(jmsDispatcher);
    // TODO kirk 2009-10-30 -- Need exception listener.
    
    _listenerContainersBySpec.put(tickDistributionSpecification, listenerContainer);
    
    listenerContainer.afterPropertiesSet();
    listenerContainer.start();
  }

  @Override
  public boolean isRunning() {
    return _running.get();
  }

  @Override
  public void start() {
    _running.set(true);
  }

  @Override
  public synchronized void stop() {
    for(Map.Entry<String, DefaultMessageListenerContainer> entry : _listenerContainersBySpec.entrySet()) {
      s_logger.info("Shutting down listener container on topic {}", entry.getKey());
      entry.getValue().stop();
      entry.getValue().destroy();
    }
    _listenerContainersBySpec.clear();
    _running.set(false);
  }

}
