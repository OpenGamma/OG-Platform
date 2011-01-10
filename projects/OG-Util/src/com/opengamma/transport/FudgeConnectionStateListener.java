/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * Callback interface to receive updates on a connection's state.
 */
public interface FudgeConnectionStateListener {

  /**
   * The underlying transport has been dropped and reconnected. E.g. if this is a client,
   * the server is now seeing it from a new connection.
   * 
   * @param connection the affected connection
   */
  void connectionReset(FudgeConnection connection);

  /**
   * The underlying transport is no longer available and messages cannot be sent nor
   * received.
   * 
   * @param connection the affected connection
   * @param cause the exception if one was thrown, or {@code null} otherwise
   */
  void connectionFailed(FudgeConnection connection, Exception cause);

}
