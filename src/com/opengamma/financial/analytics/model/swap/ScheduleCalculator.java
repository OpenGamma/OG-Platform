/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.ZonedDateTime;
import javax.time.period.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 *
 */
public class ScheduleCalculator {
  private static final ZonedDateTime[] EMPTY_ARRAY = new ZonedDateTime[0];

  public static double[] getPaySchedule(final SwapSecurity security, final Calendar calendar) {
    return getSchedule(security.getEffectiveDate(), security.getMaturityDate(), security.getPayLeg(), calendar);
  }

  public static double[] getReceiveSchedule(final SwapSecurity security, final Calendar calendar) {
    return getSchedule(security.getEffectiveDate(), security.getMaturityDate(), security.getReceiveLeg(), calendar);
  }

  public static double[] getSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final SwapLeg leg, final Calendar calendar) {
    final Frequency payFrequency = leg.getFrequency();
    final ZonedDateTime[] unadjusted = getUnadjustedDates(effectiveDate, maturityDate, payFrequency);
    final ZonedDateTime[] adjusted = getAdjustedDates(unadjusted, leg.getBusinessDayConvention(), calendar);
    return getPaymentTimes(adjusted, leg.getDaycount());
  }

  public static ZonedDateTime[] getUnadjustedDates(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final Frequency frequency) {
    if (!(frequency instanceof PeriodFrequency)) {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency");
    }
    final Period period = ((PeriodFrequency) frequency).getPeriod();
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    dates.add(effectiveDate); //TODO change when we put the distinction between the swap start date and the accrual start date
    ZonedDateTime date = effectiveDate;
    while (date.isBefore(maturityDate)) { //REVIEW: could speed this up by working out how many periods between start and end date?
      date = date.plus(period);
      dates.add(date);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  public static ZonedDateTime[] getAdjustedDates(final ZonedDateTime[] dates, final BusinessDayConvention convention,
      final Calendar calendar) {
    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      result[i] = convention.adjustDate(calendar, dates[i]);
    }
    return result;
  }

  public static double[] getPaymentTimes(final ZonedDateTime[] dates, final DayCount dayCount) {
    final int n = dates.length;
    final double[] result = new double[n - 1];
    for (int i = 1; i < n; i++) {
      result[i - 1] = dayCount.getDayCountFraction(dates[i - 1], dates[i]);
    }
    return result;
  }
}
