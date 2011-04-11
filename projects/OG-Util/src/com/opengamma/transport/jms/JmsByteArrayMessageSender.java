/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

/**
 * A message sender that uses JMS.
 * <p>
 * This is a simple implementation based on JMS.
 */
public class JmsByteArrayMessageSender extends AbstractJmsByteArraySender implements ByteArrayMessageSender {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageSender.class);

  /**
   * Creates an instance associated with a destination and template.
   * 
   * @param destinationName  the destination name, not null
   * @param jmsTemplate  the template, not null
   */
  public JmsByteArrayMessageSender(final String destinationName, final JmsTemplate jmsTemplate) {
    super(destinationName, jmsTemplate);
  }

  //-------------------------------------------------------------------------
  @Override
  public void send(final byte[] message) {
    s_logger.debug("Sending message size {} to {}", message.length, getDestinationName());
    getJmsTemplate().send(getDestinationName(), new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
        final BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(message);
        return bytesMessage;
      }
    });
  }

}
