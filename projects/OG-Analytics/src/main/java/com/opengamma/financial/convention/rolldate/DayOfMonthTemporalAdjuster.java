/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.util.ArgumentChecker;

/**
 *  Adjusts a date to the day of a month
 */
public class DayOfMonthTemporalAdjuster implements TemporalAdjuster {

  /** 
   * day of a month
   */
  private final int _day;

  /**
   * Public constructor.
   * @param day day of a month
   */
  public DayOfMonthTemporalAdjuster(final int day) {
    ArgumentChecker.notNull(day, "day");
    ArgumentChecker.isTrue(0 < day & day < 32, "day should be between 1 and 31");
    _day = day;
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    ArgumentChecker.notNull(temporal, "temporal");
    // simple catch for Feb, need to handle the general case
    if (temporal.get(ChronoField.MONTH_OF_YEAR) == 2 && _day > 28) {
      return temporal.with(ChronoField.DAY_OF_MONTH, 28);
    }
    return temporal.with(ChronoField.DAY_OF_MONTH, _day);
  }

}
