/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.util.time.DateUtils;

/**
 * Utility class for building date constraints for the time series fetching functions.
 */
public class DateConstraint {

  /**
   * Date constraint referring to the earliest start date of a time series.
   */
  public static final DateConstraint EARLIEST_START = new DateConstraint("");
  /**
   * Date constraint referring to the current valuation time.
   */
  public static final ValuationTime VALUATION_TIME = new ValuationTime();

  private static final String PREVIOUS_WEEK_DAY = "PreviousWeekDay";

  private final String _value;

  /* package */DateConstraint(final String value) {
    _value = value;
  }

  public static DateConstraint of(final LocalDate date) {
    return new DateConstraint(date.toString());
  }

  /**
   * Date constraint representing the current valuation time.
   */
  public static final class ValuationTime extends DateConstraint {

    private ValuationTime() {
      super("");
    }

    public ValuationTimeMinus minus(final Period period) {
      return new ValuationTimeMinus(period.toString());
    }

    public ValuationTimeMinus minus(final String periodString) {
      return new ValuationTimeMinus(periodString);
    }

    public ValuationTimeMinus yesterday() {
      return new ValuationTimeMinus("P1D");
    }

    public DateConstraint previousWeekDay() {
      return new DateConstraint(PREVIOUS_WEEK_DAY);
    }

  }

  /**
   * Date constraint representing the current valuation time, minus a fixed period.
   */
  public static final class ValuationTimeMinus extends DateConstraint {

    private ValuationTimeMinus(final String period) {
      super("-" + period);
    }

    public DateConstraint previousWeekDay() {
      return new DateConstraint(PREVIOUS_WEEK_DAY + "(" + toString() + ")");
    }

  }

  protected static LocalDate getLocalDate(final FunctionExecutionContext context, final String str) {
    if (str.length() == 0) {
      return null;
    } else if (str.charAt(0) == '-') {
      return context.getValuationClock().today().minus(Period.parse(str.substring(1)));
    } else if (str.startsWith(PREVIOUS_WEEK_DAY)) {
      final int l = str.length();
      final int pwd = PREVIOUS_WEEK_DAY.length();
      if (l == pwd) {
        return DateUtils.previousWeekDay(context.getValuationClock().today());
      } else {
        return DateUtils.previousWeekDay(getLocalDate(context, str.substring(pwd + 1, l - 1)));
      }
    } else {
      return LocalDate.parse(str);
    }
  }

  @Override
  public String toString() {
    return _value;
  }

}
