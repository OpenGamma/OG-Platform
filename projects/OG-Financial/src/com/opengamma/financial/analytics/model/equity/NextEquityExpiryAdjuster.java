/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.EnumSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import com.opengamma.financial.analytics.ircurve.NextQuarterAdjuster;

/**
 * {@code DatAdjuster} that finds the next Expiry in Equity Futures Options. 
 * This is the Saturday immediately following the 3rd Friday of the next IMM Future Expiry Month. 
 */
public class NextEquityExpiryAdjuster implements DateAdjuster {

  /** An adjuster finding the Saturday following the 3rd Friday in a month. May be before or after date */
  private static final DateAdjuster s_dayOfMonth = new SaturdayAfterThirdFridayAdjuster();
  
  /** An adjuster moving to the next quarter */
  private static final DateAdjuster s_nextQuarterAdjuster = new NextQuarterAdjuster();
  
  /** The IMM Expiry months  */
  private final Set<MonthOfYear> _immFutureQuarters = EnumSet.of(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    if (_immFutureQuarters.contains(date.getMonthOfYear()) &&
        date.with(s_dayOfMonth).isAfter(date)) { // in a quarter
      return date.with(s_dayOfMonth);
    } else {
      return date.with(s_nextQuarterAdjuster).with(s_dayOfMonth);
    }
  }

}
