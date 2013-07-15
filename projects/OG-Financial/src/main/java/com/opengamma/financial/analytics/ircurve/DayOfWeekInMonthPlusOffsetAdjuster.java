/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 *
 */
public class DayOfWeekInMonthPlusOffsetAdjuster implements TemporalAdjuster {

  private final int _week;
  private final DayOfWeek _day;
  private final int _offset;

  public DayOfWeekInMonthPlusOffsetAdjuster(int week, DayOfWeek day, int offset) {
    _week = week;
    _day = day;
    _offset = offset;
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    final TemporalAdjuster unadjustedDayInMonth = TemporalAdjusters.dayOfWeekInMonth(_week, _day);
    return temporal.with(unadjustedDayInMonth).plus(_offset, DAYS);
  }
}
