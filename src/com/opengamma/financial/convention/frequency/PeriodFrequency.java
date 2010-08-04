/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import javax.time.calendar.Period;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class PeriodFrequency implements Frequency {
  /**
   * A frequency with a period of one year
   */
  public static final PeriodFrequency ANNUAL = new PeriodFrequency(ANNUAL_NAME, Period.ofYears(1));
  /**
   * A frequency with a period of six months
   */
  public static final PeriodFrequency SEMI_ANNUAL = new PeriodFrequency(SEMI_ANNUAL_NAME, Period.ofMonths(6));
  /**
   * A frequency with a period of three months
   */
  public static final PeriodFrequency QUARTERLY = new PeriodFrequency(QUARTERLY_NAME, Period.ofMonths(3));
  /**
   * A frequency with a period of two months
   */
  public static final PeriodFrequency BIMONTHLY = new PeriodFrequency(BIMONTHLY_NAME, Period.ofMonths(2));
  /**
   * A frequency with a period of one month
   */
  public static final PeriodFrequency MONTHLY = new PeriodFrequency(MONTHLY_NAME, Period.ofMonths(1));
  /**
   * A frequency with a period of two weeks
   */
  public static final PeriodFrequency BIWEEKLY = new PeriodFrequency(BIWEEKLY_NAME, Period.ofDays(14));
  /**
   * A frequency with a period of one week
   */
  public static final PeriodFrequency WEEKLY = new PeriodFrequency(WEEKLY_NAME, Period.ofDays(7));
  /**
   * A frequency with a period of one day
   */
  public static final PeriodFrequency DAILY = new PeriodFrequency(DAILY_NAME, Period.ofDays(1));
  /**
   * A frequency with a period of zero (i.e. continuous)
   */
  public static final PeriodFrequency CONTINUOUS = new PeriodFrequency(CONTINUOUS_NAME, Period.ZERO);
  private final String _name;
  private final Period _period;

  protected PeriodFrequency(final String name, final Period period) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(period, "period");
    _name = name;
    _period = period;
  }

  @Override
  public String getConventionName() {
    return _name;
  }

  public Period getPeriod() {
    return _period;
  }

  public PeriodFrequency of(final String name, final Period period) {
    return new PeriodFrequency(name, period);
  }

  @Override
  public String toString() {
    return "Frequency[" + "name =  " + _name + " period = " + _period + "]";
  }
}
