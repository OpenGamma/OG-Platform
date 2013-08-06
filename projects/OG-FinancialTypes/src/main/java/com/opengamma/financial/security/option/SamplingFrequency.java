/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * The FX barrier option sampling frequency.
 */
public enum SamplingFrequency {

  /**
   * Daily close.
   */
  DAILY_CLOSE,
  /**
   * Friday.
   */
  FRIDAY,
  /**
   * Weekly close.
   */
  WEEKLY_CLOSE,
  /**
   * Continuous (for continuous monitoring)
   */
  CONTINUOUS,
  /**
   * One Look (for barriers that only apply at expiry)
   */
  ONE_LOOK

}
