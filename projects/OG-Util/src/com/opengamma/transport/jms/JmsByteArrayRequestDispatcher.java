/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.SessionAwareMessageListener;

import com.opengamma.transport.ByteArrayRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class JmsByteArrayRequestDispatcher implements SessionAwareMessageListener<Message> {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayRequestDispatcher.class);
  private final ByteArrayRequestReceiver _underlying;
  
  public JmsByteArrayRequestDispatcher(ByteArrayRequestReceiver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  /**
   * @return the underlying
   */
  public ByteArrayRequestReceiver getUnderlying() {
    return _underlying;
  }

  @Override
  public void onMessage(Message message, Session session) throws JMSException {
    Destination replyTo = message.getJMSReplyTo();
    if (replyTo == null) {
      throw new IllegalArgumentException("No JMSReplyTo destination set.");
    }
    byte[] requestBytes = JmsByteArrayHelper.extractBytes(message);
    s_logger.debug("Dispatching request {} of size {} to underlying", message.getJMSMessageID(), requestBytes.length);
    byte[] responseBytes = getUnderlying().requestReceived(requestBytes);
    s_logger.debug("Returning response of size {} to {}", responseBytes.length, replyTo);
    MessageProducer mp = session.createProducer(replyTo);
    BytesMessage bytesMessage = session.createBytesMessage();
    bytesMessage.writeBytes(responseBytes);
    bytesMessage.setJMSCorrelationID(message.getJMSMessageID());
    mp.send(bytesMessage);
    mp.close();
  }

}
