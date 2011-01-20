/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
    ByteArrayMessageReceiver, BatchByteArrayMessageReceiver {
  private final List<byte[]> _messages = Collections.synchronizedList(new LinkedList<byte[]>());

  /**
   * @return the messages
   */
  public List<byte[]> getMessages() {
    return _messages;
  }
  
  public void clearMessages() {
    _messages.clear();
  }

  @Override
  public void messageReceived(byte[] message) {
    getMessages().add(message);
  }

  @Override
  public void messagesReceived(List<byte[]> messages) {
    getMessages().addAll(messages);
  }

}
