/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * Allows code to send messages, and receive single responses.
 * This follows the messaging RPC pattern, where a temporary topic/queue
 * is opened to receive a response to a particular request.
 *
 * @author kirk
 */
public interface ByteArrayRequestSender {

  void sendRequest(byte[] request, ByteArrayMessageReceiver responseReceiver);
}
