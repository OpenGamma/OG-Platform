/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Simply collects the messages sent and allows them to be retrieved.
 * This implementation of {@link ByteArrayMessageSender} is primarily useful
 * for testing purposes.
 *
 * @author kirk
 */
public class CollectingByteArrayMessageSender implements ByteArrayMessageSender {
  private final List<byte[]> _sentMessages = new ArrayList<byte[]>();

  @Override
  public synchronized void send(byte[] message) {
    _sentMessages.add(message);
  }
  
  public synchronized void clear() {
    _sentMessages.clear();
  }
  
  public synchronized List<byte[]> getMessages() {
    return new ArrayList<byte[]>(_sentMessages);
  }

}
