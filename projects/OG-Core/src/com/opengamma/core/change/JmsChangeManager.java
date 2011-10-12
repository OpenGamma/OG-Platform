/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Manager for receiving and handling entity change events.
 * <p>
 * Events are sent when an entity is added, updated, removed or corrected.
 * <p>
 * This class is mutable and thread-safe using concurrent collections.
 */
@PublicSPI
public class JmsChangeManager extends BasicChangeManager implements MessageListener, FudgeMessageReceiver, Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsChangeManager.class);

  /**
   * The JMS template, not null
   */
  private final JmsTemplate _jmsTemplate;
  /**
   * The JMS topic, not null
   */
  private final String _topic;
  /**
   * The JMS message dispatcher.
   */
  private final JmsByteArrayMessageDispatcher _messageDispatcher;

  private final ConnectionFactory _connectionFactory;

  private Connection _connection;

  /**
   * Creates a manager.
   * @param connectionFactory the connectionFactory to use to send and receive events
   * @param topic the topic to send events to
   */
  public JmsChangeManager(ConnectionFactory connectionFactory, String topic) {
    
    _connectionFactory = connectionFactory;
    _topic = topic;
    
    //Hook up our two message interfaces
    _jmsTemplate = new JmsTemplate();
    _jmsTemplate.setPubSubDomain(true);
    _jmsTemplate.setConnectionFactory(connectionFactory);
    ByteArrayFudgeMessageReceiver bafmr = new ByteArrayFudgeMessageReceiver(this, OpenGammaFudgeContext.getInstance());
    _messageDispatcher = new JmsByteArrayMessageDispatcher(bafmr);
  }

  @Override
  public void start() {
    try {
      _connection = _connectionFactory.createConnection();
      _connection.start();
      Session session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = session.createTopic(_topic);
      
      MessageConsumer messageConsumer = session.createConsumer(topic);
      messageConsumer.setMessageListener(this);
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create change manager for topic " + _topic, e);      
    }
  }

  @Override
  public void stop() {
    try {
      _connection.stop();
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to stop change manager for topic " + _topic, e);
    }
    _connection = null;
  }
  
  @Override
  public boolean isRunning() {
    return _connection != null;
  }

  
  //-------------------------------------------------------------------------
  /**
   * Gets the JMS template.
   * 
   * @return the JMS template, not null
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * Gets the JMS topic.
   * 
   * @return the JMS topic, not null
   */
  public String getTopic() {
    return _topic;
  }

  //-------------------------------------------------------------------------
  /**
   * Handles an event when an entity changes.
   * <p>
   * This implementation sends the event by JMS to be received by all change
   * managers, including this one.
   * 
   * @param event  the event that occurred, not null
   */
  @Override
  protected void handleEntityChanged(final ChangeEvent event) {
    FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(event);
    s_logger.debug("Source changed: Sending message {}", msg);
    byte[] fudgeMsg = OpenGammaFudgeContext.getInstance().toByteArray(msg.getMessage());
    JmsByteArrayMessageSender messageSender = new JmsByteArrayMessageSender(getTopic(), getJmsTemplate());
    messageSender.send(fudgeMsg);
  }

  @Override
  public void onMessage(Message message) {
    _messageDispatcher.onMessage(message);
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    FudgeMsg msg = msgEnvelope.getMessage();
    s_logger.debug("Source changed: Received message {}", msg);
    FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    ChangeEvent event = deserializer.fudgeMsgToObject(ChangeEvent.class, msg);
    fireEntityChanged(event);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a debugging string for the manager.
   * 
   * @return the debugging string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
