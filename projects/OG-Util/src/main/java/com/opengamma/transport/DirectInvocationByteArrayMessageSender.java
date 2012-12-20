/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Directly invokes {@link ByteArrayMessageReceiver} instances whenever a message is transmitted.
 * This is ideally useful in integration tests where all operations should be in
 * a single virtual memory instance.
 * Because this happens in the caller's thread context, it's not particularly scalable.
 */
public class DirectInvocationByteArrayMessageSender implements ByteArrayMessageSender {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DirectInvocationByteArrayMessageSender.class);

  /**
   * The list of receivers. Not synchronized!
   */
  private final List<ByteArrayMessageReceiver> _receivers = new ArrayList<ByteArrayMessageReceiver>();

  /**
   * Creates a message sender.
   */
  public DirectInvocationByteArrayMessageSender() {
  }

  /**
   * Creates a message sender with one receiver.
   * @param receiver  the receiver to use, not null
   */
  public DirectInvocationByteArrayMessageSender(ByteArrayMessageReceiver receiver) {
    addReceiver(receiver);
  }

  /**
   * Creates a message sender adding receivers.
   * @param receivers  the receivers to use, not null, no nulls
   */
  public DirectInvocationByteArrayMessageSender(ByteArrayMessageReceiver... receivers) {
    ArgumentChecker.notNull(receivers, "receivers");
    for (ByteArrayMessageReceiver receiver : receivers) {
      addReceiver(receiver);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a receiver to the list to call.
   * @param receiver  the receiver to add, not null
   */
  public synchronized void addReceiver(ByteArrayMessageReceiver receiver) {
    ArgumentChecker.notNull(receiver, "receiver");
    _receivers.add(receiver);
  }

  //-------------------------------------------------------------------------
  /**
   * Sends a message to each of the registered receivers.
   * @param message  the message to send, not null
   */
  @Override
  public synchronized void send(byte[] message) {
    for (ByteArrayMessageReceiver receiver : _receivers) {
      try {
        receiver.messageReceived(message);
      } catch (Exception e) {
        s_logger.warn("Unable to dispatch message to receiver " + receiver, e);
      }
    }
  }

}
