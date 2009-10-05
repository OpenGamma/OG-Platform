/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

/**
 * Specifies the outcome of attempting to invoke an analytic function
 * by a Calculation Node.
 *
 * @author kirk
 */
public enum InvocationResult {
  SUCCESS,
  TIMED_OUT,
  ERROR;

}
