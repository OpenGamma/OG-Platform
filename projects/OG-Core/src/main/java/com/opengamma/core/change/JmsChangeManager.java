/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import javax.jms.Connection;
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

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
   * The JMS connector, not null
   */
  private final JmsConnector _jmsConnector;
  /**
   * The JMS message dispatcher.
   */
  private final JmsByteArrayMessageDispatcher _messageDispatcher;
  /**
   * The connection.
   */
  private volatile Connection _connection;

  /**
   * Creates a change manager.
   * <p>
   * The topic name will be defaulted to the name of this class if not set.
   * 
   * @param connector  the JMS connector, not null
   */
  public JmsChangeManager(JmsConnector connector) {
    ArgumentChecker.notNull(connector, "connector");
    _jmsConnector = connector.ensureTopicName();
    ByteArrayFudgeMessageReceiver bafmr = new ByteArrayFudgeMessageReceiver(this, OpenGammaFudgeContext.getInstance());
    _messageDispatcher = new JmsByteArrayMessageDispatcher(bafmr);
  }

  /**
   * Creates a change manager.
   * 
   * @param connector  the JMS connector, not null
   * @param topicName  the topic name to use, not null
   */
  public JmsChangeManager(JmsConnector connector, String topicName) {
    ArgumentChecker.notNull(connector, "connector");
    _jmsConnector = connector.withTopicName(topicName);
    ByteArrayFudgeMessageReceiver bafmr = new ByteArrayFudgeMessageReceiver(this, OpenGammaFudgeContext.getInstance());
    _messageDispatcher = new JmsByteArrayMessageDispatcher(bafmr);
  }

  //-------------------------------------------------------------------------
  @Override
  public void start() {
    final String topicName = _jmsConnector.getTopicName();
    try {
      _connection = _jmsConnector.getConnectionFactory().createConnection();
      _connection.start();
      final Session session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      final Topic topic = session.createTopic(topicName);
      final MessageConsumer messageConsumer = session.createConsumer(topic);
      messageConsumer.setMessageListener(this);
      
    } catch (JMSException ex) {
      throw new OpenGammaRuntimeException("Failed to create change manager on topic: " + topicName, ex);
    }
  }

  @Override
  public void stop() {
    final String topicName = _jmsConnector.getTopicName();
    try {
      _connection.close();
      _connection = null;
      
    } catch (JMSException ex) {
      throw new OpenGammaRuntimeException("Failed to stop change manager on topic: " + topicName, ex);
    }
  }

  @Override
  public boolean isRunning() {
    return _connection != null;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * 
   * @return the JMS connector, not null
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
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
    final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(event);
    s_logger.debug("Sending change message {}", msg);
    final byte[] fudgeMsg = OpenGammaFudgeContext.getInstance().toByteArray(msg.getMessage());
    final JmsByteArrayMessageSender messageSender = new JmsByteArrayMessageSender(getJmsConnector().getTopicName(), getJmsConnector().getJmsTemplateTopic());
    messageSender.send(fudgeMsg);
  }

  @Override
  public void onMessage(Message message) {
    try {
      _messageDispatcher.onMessage(message);
    } catch (Exception e) {
      // NOTE jonathan 2013-06-05 -- it's an error to throw an exception in onMessage and may cause messages to back up
      // in the JMS broker which could affect its stability. Drop the message.
      s_logger.error("Error processing JMS message", e);
    }
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    final FudgeMsg msg = msgEnvelope.getMessage();
    s_logger.debug("Received change message {}", msg);
    final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    final ChangeEvent event = deserializer.fudgeMsgToObject(ChangeEvent.class, msg);
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
