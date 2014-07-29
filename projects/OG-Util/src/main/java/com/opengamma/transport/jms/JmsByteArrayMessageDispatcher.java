/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * A message dispatcher that uses JMS.
 * <p>
 * This is a simple implementation based on JMS.
 */
public class JmsByteArrayMessageDispatcher implements MessageListener {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageDispatcher.class);

  /**
   * The underlying message receiver.
   */
  private final ByteArrayMessageReceiver _underlying;

  /**
   * Creates an instance based on a message receiver.
   * 
   * @param underlying the underlying message receiver, not null
   */
  public JmsByteArrayMessageDispatcher(final ByteArrayMessageReceiver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying message receiver.
   * 
   * @return the underlying message receiver
   */
  public ByteArrayMessageReceiver getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public void onMessage(final Message message) {
    try {
      final byte[] bytes = JmsByteArrayHelper.extractBytes(message);
      s_logger.debug("Dispatching byte array of length {}", bytes.length);
      getUnderlying().messageReceived(bytes);
    } catch (RuntimeException e) {
      s_logger.error("Caught exception dispatching message", e);
      throw e;
    }
  }

}
