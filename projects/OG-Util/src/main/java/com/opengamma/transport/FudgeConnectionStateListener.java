/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * Callback interface to receive updates on a connection's state.
 */
public interface FudgeConnectionStateListener {

  /**
   * Callback used when the underlying transport has been dropped and reconnected.
   * For example, if this is a client, the server is now seeing it from a new connection.
   * 
   * @param connection  the affected connection
   */
  void connectionReset(FudgeConnection connection);

  /**
   * Callback used when the underlying transport is no longer available
   * and messages cannot be sent nor received.
   * 
   * @param connection  the affected connection
   * @param cause  the exception if one was thrown, may be null
   */
  void connectionFailed(FudgeConnection connection, Exception cause);

}
