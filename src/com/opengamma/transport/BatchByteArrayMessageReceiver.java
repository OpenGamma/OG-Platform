/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.List;

/**
 * An interface through which code can receive blocks of messages to operate
 * on them as one unit.
 *
 * @author kirk
 */
public interface BatchByteArrayMessageReceiver {
  
  /**
   * Invoked when at least one message has been received.
   * Messages are provided in the order originally received.
   * 
   * @param messages The messages received by the underlying transport handler.
   */
  void messagesReceived(List<byte[]> messages);

}
