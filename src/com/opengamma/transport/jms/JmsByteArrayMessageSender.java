/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class JmsByteArrayMessageSender implements ByteArrayMessageSender {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageSender.class);
  private final String _destinationName;
  private final JmsTemplate _jmsTemplate;
  
  public JmsByteArrayMessageSender(String destinationName, JmsTemplate jmsTemplate) {
    ArgumentChecker.checkNotNull(destinationName, "JMS Destination Name");
    ArgumentChecker.checkNotNull(jmsTemplate, "JmsTemplate");
    _destinationName = destinationName;
    _jmsTemplate = jmsTemplate;
  }

  /**
   * @return the destinationName
   */
  public String getDestinationName() {
    return _destinationName;
  }

  /**
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  @Override
  public void send(final byte[] message) {
    s_logger.debug("Sending message size {} to {}", message.length, getDestinationName());
    getJmsTemplate().send(getDestinationName(), new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(message);
        return bytesMessage;
      }
    });
  }

}
