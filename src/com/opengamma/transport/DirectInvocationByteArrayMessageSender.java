/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Directly invokes {@link ByteArrayMessageReceiver} instances whenever a message
 * is transmitted.
 * This is ideally useful in integration tests where all operations should be in
 * a single virtual memory instance.
 * Because this happens in the caller's thread context, it's not particularly
 * scalable.
 *
 * @author kirk
 */
public class DirectInvocationByteArrayMessageSender implements ByteArrayMessageSender {
  private static final Logger s_logger = LoggerFactory.getLogger(DirectInvocationByteArrayMessageSender.class);
  private final List<ByteArrayMessageReceiver> _receivers = new ArrayList<ByteArrayMessageReceiver>();
  
  public DirectInvocationByteArrayMessageSender() {
  }
  
  public DirectInvocationByteArrayMessageSender(ByteArrayMessageReceiver receiver) {
    addReceiver(receiver);
  }

  public DirectInvocationByteArrayMessageSender(ByteArrayMessageReceiver... receivers) {
    ArgumentChecker.notNull(receivers, "Receivers");
    for(ByteArrayMessageReceiver receiver : receivers) {
      addReceiver(receiver);
    }
  }

  /**
   * @param receiver
   */
  public synchronized void addReceiver(ByteArrayMessageReceiver receiver) {
    ArgumentChecker.notNull(receiver, "Receiver");
    _receivers.add(receiver);
  }

  @Override
  public synchronized void send(byte[] message) {
    for(ByteArrayMessageReceiver receiver : _receivers) {
      try {
        receiver.messageReceived(message);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch message to receiver " + receiver, e);
      }
    }
  }

}
