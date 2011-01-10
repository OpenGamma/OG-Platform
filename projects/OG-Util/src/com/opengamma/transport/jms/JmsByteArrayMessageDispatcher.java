/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
 * 
 *
 * @author kirk
 */
public class JmsByteArrayMessageDispatcher implements MessageListener {
  private static final Logger s_logger = LoggerFactory.getLogger(JmsByteArrayMessageDispatcher.class);
  private final ByteArrayMessageReceiver _underlying;
  
  public JmsByteArrayMessageDispatcher(ByteArrayMessageReceiver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
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
    byte[] bytes = JmsByteArrayHelper.extractBytes(message);
    s_logger.debug("Dispatching byte array of length {}", bytes.length);
    getUnderlying().messageReceived(bytes);
  }

}
