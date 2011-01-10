/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * 
 *
 * @author kirk
 */
public final class JmsByteArrayHelper {
  private JmsByteArrayHelper() {
  }

  public static byte[] extractBytes(Message message) {
    if (!(message instanceof BytesMessage)) {
      throw new IllegalArgumentException("JmsByteArrayMessageDispatcher can only dispatch BytesMessage instances.");
    }
    BytesMessage bytesMessage = (BytesMessage) message;
    byte[] bytes = null;
    try {
      long bodyLength = bytesMessage.getBodyLength();
      if (bodyLength > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Can only dispatch 2GB messages. Received one of length " + bodyLength);
      }
      bytes = new byte[(int) bodyLength];
      bytesMessage.readBytes(bytes);
    } catch (JMSException jmse) {
      throw new JmsRuntimeException(jmse);
    }
    return bytes;
  }
}
