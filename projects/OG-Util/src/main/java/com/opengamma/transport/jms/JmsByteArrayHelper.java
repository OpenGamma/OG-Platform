/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Helper class to aid with the use of JMS.
 */
public final class JmsByteArrayHelper {

  /**
   * Restricted constructor.
   */
  private JmsByteArrayHelper() {
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the byte array from a JMS message.
   * 
   * @param message  the JMS message, not null
   * @return the extracted byte array, not null
   */
  public static byte[] extractBytes(final Message message) {
    if (message instanceof BytesMessage == false) {
      throw new IllegalArgumentException("Message must be an instanceof BytesMessage");
    }
    final BytesMessage bytesMessage = (BytesMessage) message;
    final byte[] bytes;
    try {
      long bodyLength = bytesMessage.getBodyLength();
      if (bodyLength > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Message too large, maximum size is 2GB, received one of length " + bodyLength);
      }
      bytes = new byte[(int) bodyLength];
      bytesMessage.readBytes(bytes);
    } catch (JMSException jmse) {
      throw new JmsRuntimeException(jmse);
    }
    return bytes;
  }

}
