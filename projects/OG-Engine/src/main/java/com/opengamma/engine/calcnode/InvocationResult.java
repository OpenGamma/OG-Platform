/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

/**
 * Specifies the outcome of attempting to invoke an analytic function
 * by a Calculation Node.
 */
public enum InvocationResult {
  /**
   * The invocation was not attempted because of a function blacklisting rule.
   */
  SUPPRESSED,
  /**
   * The invocation was completely successful.
   */
  SUCCESS,
  /**
   * The invocation completed but did not produce all expected results.
   */
  PARTIAL_SUCCESS,
  /**
   * The invocation failed with the function throwing an exception.
   */
  FUNCTION_THREW_EXCEPTION,
  /**
   * The invocation was not attempted because one or more inputs were missing.
   */
  MISSING_INPUTS;

}
