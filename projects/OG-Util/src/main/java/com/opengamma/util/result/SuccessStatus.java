/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * Represents the status of a function call which has completed successfully.
 * <p>
 * A successful result always contains a non-null value.
 */
public enum SuccessStatus implements ResultStatus {
  // there will probably never be a need to add another status type

  /**
   * The result contains a successfully calculated value.
   */
  SUCCESS;

  //-------------------------------------------------------------------------
  /**
   * Returns true to indicate that a result is available.
   *
   * @return true
   */
  public boolean isResultAvailable() {
    return true;
  }

}
