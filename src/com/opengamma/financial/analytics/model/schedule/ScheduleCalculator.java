/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;

/**
 * 
 *
 */
public class ScheduleCalculator {
  private static final ZonedDateTime[] EMPTY_ARRAY = new ZonedDateTime[0];

  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    return getUnadjustedDates(effectiveDate, effectiveDate, maturityDate, frequency);
  }

  public static ZonedDateTime[] getUnadjustedDates(final ZonedDateTime effectiveDate, final ZonedDateTime accrualDate,
      final ZonedDateTime maturityDate, final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(accrualDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }
    //TODO what if there's no valid date between accrual date and maturity date?
    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = effectiveDate; //TODO this is only correct if effective date = maturity date
    while (date.isBefore(maturityDate)) { //REVIEW: could speed this up by working out how many periods between start and end date?
      date = date.plus(period);
      dates.add(date);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention,
      final Calendar calendar) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      result[i] = convention.adjustDate(calendar, dates[i]);
    }
    return result;
  }

  public static double[] getTimes(final ZonedDateTime[] dates, final DayCount dayCount) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(dayCount);
    final int n = dates.length;
    final double[] result = new double[n - 1];
    for (int i = 1; i < n; i++) {
      result[i - 1] = dayCount.getDayCountFraction(dates[i - 1], dates[i]);
    }
    return result;
  }
}
