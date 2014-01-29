/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * Represents the status of a function call, such that clients can use
 * it to determine whether they can continue, or need to pass failure
 * to their callers.
 */
public interface ResultStatus {

  /**
   * Indicates if a Result with this status has a return value populated.
   *
   * @return true if the Result has its return value populated
   */
  boolean isResultAvailable();

}
