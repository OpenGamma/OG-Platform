/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * Represents the status of a function call which has completed successfully.
 */
public enum SuccessStatus implements ResultStatus {

  /**
   * The function call completed successfully.
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
