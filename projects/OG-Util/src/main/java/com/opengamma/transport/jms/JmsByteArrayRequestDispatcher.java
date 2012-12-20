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
 * A request dispatcher that uses JMS.
 * <p>
 * This is a simple implementation based on JMS.
 */
public class JmsByteArrayRequestDispatcher implements SessionAwareMessageListener<Message> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayRequestDispatcher.class);

  /**
   * The underlying request receiver.
   */
  private final ByteArrayRequestReceiver _underlying;

  /**
   * Creates an instance based on a request receiver.
   * 
   * @param underlying  the underlying request receiver, not null
   */
  public JmsByteArrayRequestDispatcher(final ByteArrayRequestReceiver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * @return the underlying
   */
  public ByteArrayRequestReceiver getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    final Destination replyTo = message.getJMSReplyTo();
    if (replyTo == null) {
      throw new IllegalArgumentException("No JMSReplyTo destination set.");
    }
    final byte[] requestBytes = JmsByteArrayHelper.extractBytes(message);
    s_logger.debug("Dispatching request {} of size {} to underlying", message.getJMSMessageID(), requestBytes.length);
    final byte[] responseBytes = getUnderlying().requestReceived(requestBytes);
    s_logger.debug("Returning response of size {} to {}", responseBytes.length, replyTo);
    final MessageProducer mp = session.createProducer(replyTo);
    try {
      final BytesMessage bytesMessage = session.createBytesMessage();
      bytesMessage.writeBytes(responseBytes);
      bytesMessage.setJMSCorrelationID(message.getJMSMessageID());
      mp.send(bytesMessage);
    } finally {
      mp.close();
    }
  }

}
