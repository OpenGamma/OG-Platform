/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * The general interface through which OpenGamma code can send a byte array
 * onto any network transport.
 *
 * @author kirk
 */
public interface ByteArrayMessageSender {
  
  void send(byte[] message);

}
