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
   * The invocation failed because the function unexpected threw an exception
   */
  FUNCTION_THREW_EXCEPTION,
  /**
   * The invocation failed - in fact, could not even be attempted - because
   * one of the inputs to the function was missing 
   */
  MISSING_INPUTS;

}
