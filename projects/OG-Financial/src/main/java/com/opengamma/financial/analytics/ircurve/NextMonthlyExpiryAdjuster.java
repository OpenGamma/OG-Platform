/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.EnumSet;
import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * {@code DatAdjuster} that finds the next Expiry in Interest Rate Futures Options. 
 * This is the 3rd Wednesday of the next IMM Future Expiry Month.
 */
public class NextMonthlyExpiryAdjuster implements TemporalAdjuster {

  /** An adjuster finding the 3rd Wednesday in a month. May be before or after date */
  private static final TemporalAdjuster s_dayOfMonth = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);

  /** An adjuster moving to the next quarter. */
  private static final TemporalAdjuster s_nextMonthAdjuster = new NextMonthAdjuster();

  /** The IMM Expiry months  */
  private final Set<Month> _futureQuarters = EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);

  @Override
  public Temporal adjustInto(Temporal temporal) {
    LocalDate date = LocalDate.from(temporal);
    if (_futureQuarters.contains(date.getMonth()) &&
        date.with(s_dayOfMonth).isAfter(date)) { // in a quarter
      return temporal.with(date.with(s_dayOfMonth));
    } else {
      return temporal.with(date.with(s_nextMonthAdjuster).with(s_dayOfMonth));
    }
  }

}
