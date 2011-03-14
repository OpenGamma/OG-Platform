/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.concurrent.TimeoutException;

/**
 * Sends messages to the C++ layer.
 */
public interface MessageSender {

  /**
   * Schedules a message for asynchronous transmission and returns immediately.
   * 
   * @param message The message payload to send
   */
  void send(UserMessagePayload message);

  /**
   * Adds a message to the transmission queue and blocks until it is sent.
   * 
   * @param message The message payload to send
   * @param timeoutMillis The timeout
   * @throws TimeoutException if the timeout is exceeded before the message is sent. Note that the
   * message may still be sent.
   */
  void sendAndWait(UserMessagePayload message, long timeoutMillis) throws TimeoutException;

  /**
   * Adds a message to the transmission queue and blocks until a corresponding response is received.
   * 
   * @param message The message payload to send
   * @param timeoutMillis The timeout
   * @throws TimeoutException if the timeout is exceeded before either the message can be sent or
   * the result received. Note that the message may still have been sent and a result may be
   * received but will be discarded.
   * @return the message received in response
   */
  UserMessagePayload call(UserMessagePayload message, long timeoutMillis) throws TimeoutException;

  /**
   * Returns the default timeout that should be used in calls to {@link #sendAndWait} or {@link #call}.
   * The timeout used should be either this value, or a multiple if the operation is known to be
   * very slow or very fast.
   * 
   * @return the default timeout in milliseconds
   */
  long getDefaultTimeout();

}
