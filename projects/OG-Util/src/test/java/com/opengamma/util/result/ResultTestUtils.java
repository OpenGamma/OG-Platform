/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * Helper for testing {@link Result} instances.
 */
public class ResultTestUtils {

  private ResultTestUtils() {
  }

  /**
   * Checks whether a result is a success, if not this throws an error containing the result's failure message.
   *
   * @param result a result
   * @throws AssertionError if the result is a failure
   */
  public static void assertSuccess(Result<?> result) {
    if (!result.isSuccess()) {
      throw new AssertionError(result.getFailureMessage());
    }
  }
}
