/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 * @author kirk
 */
public class CollectingByteArrayMessageReceiver implements
    ByteArrayMessageReceiver {
  private final List<byte[]> _messages = Collections.synchronizedList(new LinkedList<byte[]>());

  /**
   * @return the messages
   */
  public List<byte[]> getMessages() {
    return _messages;
  }

  @Override
  public void messageReceived(byte[] message) {
    getMessages().add(message);
  }

}
