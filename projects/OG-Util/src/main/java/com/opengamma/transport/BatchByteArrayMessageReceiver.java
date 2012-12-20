/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.List;

/**
 * An interface through which code can receive blocks of messages to operate
 * on them as one unit.
 */
public interface BatchByteArrayMessageReceiver {

  /**
   * Receives and processes a list of byte array messages.
   * Messages are provided in the order originally received.
   * @param messages  the messages received by the underlying transport handler, not null
   */
  void messagesReceived(List<byte[]> messages);

}
