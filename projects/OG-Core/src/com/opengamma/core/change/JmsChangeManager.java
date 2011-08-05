/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;
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
public class JmsChangeManager extends BasicChangeManager implements MessageListener, FudgeMessageReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsChangeManager.class);
  /**
   * The default topic name.
   */
  private static final String DEFAULT_TOPIC = "EntityChanged";

  /**
   * The JMS template, not null
   */
  private JmsTemplate _jmsTemplate;
  /**
   * The JMS topic, not null
   */
  private String _topic = DEFAULT_TOPIC;
  /**
   * The JMS message dispatcher.
   */
  private final JmsByteArrayMessageDispatcher _messageDispatcher;

  /**
   * Creates a manager.
   */
  public JmsChangeManager() {
    ByteArrayFudgeMessageReceiver bafmr = new ByteArrayFudgeMessageReceiver(this, OpenGammaFudgeContext.getInstance());
    _messageDispatcher = new JmsByteArrayMessageDispatcher(bafmr);
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
   * Sets the JMS template.
   * 
   * @param jmsTemplate  the JMS template, not null
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    ArgumentChecker.notNull(jmsTemplate, "jmsTemplate");
    _jmsTemplate = jmsTemplate;
  }

  /**
   * Gets the JMS topic.
   * 
   * @return the JMS topic, not null
   */
  public String getTopic() {
    return _topic;
  }

  /**
   * Sets the JMS topic.
   * 
   * @param topic  the JMS topic, not null
   */
  public void setTopic(String topic) {
    ArgumentChecker.notNull(topic, "topic");
    _topic = topic;
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
    FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    ChangeEvent event = context.fudgeMsgToObject(ChangeEvent.class, msg);
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
