/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.transport.AbstractBatchMessageDispatcher;

/**
 * A batch message dispatcher that uses JMS.
 * <p>
 * This is a simple implementation based on JMS.
 */
public class JmsBatchMessageDispatcher extends AbstractBatchMessageDispatcher {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsBatchMessageDispatcher.class);

  /**
   * The byte array source.
   */
  private final JmsByteArraySource _jmsSource;

  /**
   * Creates an instance wrapping a JMS source.
   * 
   * @param jmsTemplate  the JMS template, not null
   */
  public JmsBatchMessageDispatcher(final JmsTemplate jmsTemplate) {
    this(new JmsByteArraySource(jmsTemplate));
  }

  /**
   * Creates an instance using the wrapped queue.
   * 
   * @param source  the byte array source, not null
   */
  protected JmsBatchMessageDispatcher(final JmsByteArraySource source) {
    super(source);
    _jmsSource = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS source.
   * 
   * @return the jmsSource
   */
  public JmsByteArraySource getJmsSource() {
    return _jmsSource;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void dispatchMessages(final List<byte[]> messages) {
    super.dispatchMessages(messages);
    
    switch (getJmsSource().getJmsTemplate().getSessionAcknowledgeMode()) {
      case Session.AUTO_ACKNOWLEDGE:
      case Session.DUPS_OK_ACKNOWLEDGE:
        // do nothing
        return;
      default:
        acknowledgeMessageBatch();
    }
  }

  /**
   * Calls the JMS acknowledge API, catching the exception.
   */
  private void acknowledgeMessageBatch() {
    for (Message message : getJmsSource().getLastMessageBatch()) {
      try {
        message.acknowledge();
      } catch (JMSException ex) {
        s_logger.warn("Unable to acknowledge message", ex);
      }
    }
  }

}
