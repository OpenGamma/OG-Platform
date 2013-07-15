/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/** Returns last weekday in current month */
public class LastWeekdayAdjuster implements TemporalAdjuster {

  @Override
  public Temporal adjustInto(Temporal temporal) {
    final Temporal unadjustedLastDayInMonth = temporal.with(TemporalAdjusters.lastDayOfMonth());
    DayOfWeek lastWeekday = DayOfWeek.from(unadjustedLastDayInMonth);
    if (lastWeekday.equals(DayOfWeek.SATURDAY) || lastWeekday.equals(DayOfWeek.SUNDAY)) {
      return unadjustedLastDayInMonth.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
    } else {
      return unadjustedLastDayInMonth;
    }
  }

}
