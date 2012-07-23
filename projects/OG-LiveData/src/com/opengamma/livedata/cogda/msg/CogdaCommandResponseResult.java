/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

/**
 * The generic nature of a response to a {@link CogdaCommandMessage}.
 */
public enum CogdaCommandResponseResult {
  /** The request was successful. */
  SUCCESSFUL,
  /** The client was not authorized for that request. */
  NOT_AUTHORIZED,
  /**
   * The client asked for something which the server can't process, but
   * was otherwise well-formed.
   */
  NOT_AVAILABLE,
  /** The server encountered an internal error in processing the request. */
  INTERNAL_ERROR;

}
