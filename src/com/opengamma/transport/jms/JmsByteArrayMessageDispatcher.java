/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class JmsByteArrayMessageDispatcher implements MessageListener {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageDispatcher.class);
  private final ByteArrayMessageReceiver _underlying;
  
  public JmsByteArrayMessageDispatcher(ByteArrayMessageReceiver underlying) {
    ArgumentChecker.checkNotNull(underlying, "Underlying ByteArrayMessageReceiver");
    _underlying = underlying;
  }

  /**
   * @return the underlying
   */
  public ByteArrayMessageReceiver getUnderlying() {
    return _underlying;
  }

  @Override
  public void onMessage(Message message) {
    if(!(message instanceof BytesMessage)) {
      throw new IllegalArgumentException("JmsByteArrayMessageDispatcher can only dispatch BytesMessage instances.");
    }
    BytesMessage bytesMessage = (BytesMessage) message;
    try {
      long bodyLength = bytesMessage.getBodyLength();
      if(bodyLength > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Can only dispatch 2GB messages. Received one of length " + bodyLength);
      }
      byte[] bytes = new byte[(int)bodyLength];
      bytesMessage.readBytes(bytes);
      s_logger.debug("Dispatching byte array of length {}", bodyLength);
      getUnderlying().messageReceived(bytes);
    } catch (JMSException jmse) {
      throw new JmsRuntimeException(jmse);
    }
  }

}
