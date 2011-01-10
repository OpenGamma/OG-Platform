/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * General interface through which code can receive messages sent
 * over OpenGamma transport systems.
 *
 * @author kirk
 */
public interface ByteArrayMessageReceiver {

  void messageReceived(byte[] message);
}
