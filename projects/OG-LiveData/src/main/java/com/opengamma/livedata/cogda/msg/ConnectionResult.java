/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

/**
 * The possible result types for a {@link ConnectionRequestMessage}/{@link ConnectionResponseMessage} pair.
 */
public enum ConnectionResult {
  /** The connection request was unknown to the server and was otherwise successful. */
  NEW_CONNECTION_SUCCESS,
  /** 
   * There was already a connection known to the server, and the connection is being
   * restarted successfully.
   */
  EXISTING_CONNECTION_RESTART,
  /** The connection user wasn't authorized for connection at all. */
  NOT_AUTHORIZED;

}
