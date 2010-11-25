/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

/**
 * The accuracy of an {@code Expiry}.
 */
public enum ExpiryAccuracy {

  /**
   * Accurate to a minute.
   */
  MIN_HOUR_DAY_MONTH_YEAR,
  /**
   * Accurate to an hour.
   */
  HOUR_DAY_MONTH_YEAR,
  /**
   * Accurate to a day.
   */
  DAY_MONTH_YEAR,
  /**
   * Accurate to a month.
   */
  MONTH_YEAR,
  /**
   * Accurate to a year.
   */
  YEAR,

}
