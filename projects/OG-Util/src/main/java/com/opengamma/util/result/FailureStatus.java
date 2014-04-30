/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * Represents the status of a function call which has not completed successfully.
 */
public enum FailureStatus implements ResultStatus {

  /**
   * Some data required for the function was missing and therefore it could not
   * be successfully completed.
   */
  MISSING_DATA,
  /**
   * An exception was thrown during a function and therefore it could not
   * be successfully completed.
   */
  ERROR,
  /**
   * Some aspect of the calculation in the function has failed and therefore
   * could not be completed.
   */
  CALCULATION_FAILED,
  /**
   * There were multiple failures of different types during execution of the function.
   */
  MULTIPLE,
  /**
   * Some data required for the function has been requested but not received and therefore the function
   * could not be successfully completed.
   */
  PENDING_DATA,
  /**
   * No value was provided for a non-nullable argument.
   */
  MISSING_ARGUMENT,
  /**
   * The user has insufficient permissions to view the result.
   */
  PERMISSION_DENIED;

  //-------------------------------------------------------------------------
  /**
   * Returns false to indicate that a result is not available.
   *
   * @return false
   */
  @Override
  public boolean isResultAvailable() {
    return false;
  }

}
