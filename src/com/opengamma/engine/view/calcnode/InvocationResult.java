/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

/**
 * Specifies the outcome of attempting to invoke an analytic function
 * by a Calculation Node.
 */
public enum InvocationResult {
  /**
   * The invocation was completely successful.
   */
  SUCCESS,
  /**
   * The invocation was attempted, but took too long without a successful response.
   */
  TIMED_OUT,
  /**
   * There was an error in processing the request.
   */
  ERROR;

}
