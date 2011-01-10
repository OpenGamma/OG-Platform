/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
 * 
 *
 * @author kirk
 */
public class JmsBatchMessageDispatcher extends AbstractBatchMessageDispatcher {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsBatchMessageDispatcher.class);
  private final JmsByteArraySource _jmsSource;
  
  public JmsBatchMessageDispatcher(JmsTemplate jmsTemplate) {
    this(new JmsByteArraySource(jmsTemplate));
  }
  
  protected JmsBatchMessageDispatcher(JmsByteArraySource jmsSource) {
    super(jmsSource);
    _jmsSource = jmsSource;
  }

  /**
   * @return the jmsSource
   */
  public JmsByteArraySource getJmsSource() {
    return _jmsSource;
  }

  @Override
  protected void dispatchMessages(List<byte[]> messages) {
    super.dispatchMessages(messages);
    switch(getJmsSource().getJmsTemplate().getSessionAcknowledgeMode()) {
      case Session.AUTO_ACKNOWLEDGE:
      case Session.DUPS_OK_ACKNOWLEDGE:
        // Do nothing.
        return;
      default:
        acknowledgeMessageBatch();
    }
  }

  /**
   * 
   */
  private void acknowledgeMessageBatch() {
    for (Message message : getJmsSource().getLastMessageBatch()) {
      try {
        message.acknowledge();
      } catch (JMSException e) {
        s_logger.warn("Unable to acknowledge message", e);
      }
    }
  }

}
