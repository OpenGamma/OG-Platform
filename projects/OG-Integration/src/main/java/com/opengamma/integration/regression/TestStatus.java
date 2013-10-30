/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

/** Indicates whether the tests passed, passed with warnings or failed. */
public enum TestStatus {

  /** All results matched */
  PASS,
  /** All output values matched but the value specifications didn't. */
  WARN,
  /** Some values didn't match or were missing. */
  FAIL,
  /** The tests didn't complete because of an error. */
  ERROR;

  /**
   * Returns the 'worst' of the two statuses, i.e. combining PASS and WARN will return WARN, WARN and FAIL will
   * return FAIL etc.
   * @param other Another status
   * @return The 'worst' of the two statuses
   */
  public TestStatus combine(TestStatus other) {
    if (other.ordinal() > ordinal()) {
      return other;
    } else {
      return this;
    }
  }
}
