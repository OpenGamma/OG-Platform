/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;

/**
 * 
 */
public class IRFutureOptionUtils {
  private static final DateAdjuster s_nextExpiryAdjuster = new NextExpiryAdjuster();
  private static final DateAdjuster s_firstOfMonthAdjuster = DateAdjusters.firstDayOfMonth();

  public static Double getTime(final Number x, final ZonedDateTime now) {
    final LocalDate today = now.toLocalDate();
    final int n = x.intValue();
    if (n == 1) {
      final LocalDate nextExpiry = today.with(s_nextExpiryAdjuster);
      final LocalDate previousMonday = nextExpiry.minusDays(2); //TODO this should take a calendar and do two business days, and should use a convention for the number of days
      return TimeCalculator.getTimeBetween(today, previousMonday);
    }
    final LocalDate date = today.with(s_firstOfMonthAdjuster);
    final LocalDate plusMonths = date.plusMonths(n * 3); //TODO this is hard-coding the futures to be quarterly
    final LocalDate thirdWednesday = plusMonths.with(s_nextExpiryAdjuster);
    final LocalDate previousMonday = thirdWednesday.minusDays(2); //TODO this should take a calendar and do two business days and also use a convention for the number of days
    return TimeCalculator.getTimeBetween(today, previousMonday);

  }
}
