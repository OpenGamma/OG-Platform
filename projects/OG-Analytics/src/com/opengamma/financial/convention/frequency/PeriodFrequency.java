/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.io.Serializable;
import java.util.Map;

import javax.time.calendar.Period;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.ArgumentChecker;

/**
 * Frequency convention implementation using {@code Period}.
 */
public final class PeriodFrequency implements Frequency, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * A frequency with a period of one year.
   */
  public static final PeriodFrequency ANNUAL = new PeriodFrequency(ANNUAL_NAME, Period.ofYears(1));
  /**
   * A frequency with a period of six months.
   */
  public static final PeriodFrequency SEMI_ANNUAL = new PeriodFrequency(SEMI_ANNUAL_NAME, Period.ofMonths(6));
  /**
   * A frequency with a period of three months.
   */
  public static final PeriodFrequency QUARTERLY = new PeriodFrequency(QUARTERLY_NAME, Period.ofMonths(3));
  /**
   * A frequency with a period of two months.
   */
  public static final PeriodFrequency BIMONTHLY = new PeriodFrequency(BIMONTHLY_NAME, Period.ofMonths(2));
  /**
   * A frequency with a period of one month.
   */
  public static final PeriodFrequency MONTHLY = new PeriodFrequency(MONTHLY_NAME, Period.ofMonths(1));
  /**
   * A frequency with a period of two weeks.
   */
  public static final PeriodFrequency BIWEEKLY = new PeriodFrequency(BIWEEKLY_NAME, Period.ofDays(14));
  /**
   * A frequency with a period of one week.
   */
  public static final PeriodFrequency WEEKLY = new PeriodFrequency(WEEKLY_NAME, Period.ofDays(7));
  /**
   * A frequency with a period of one day.
   */
  public static final PeriodFrequency DAILY = new PeriodFrequency(DAILY_NAME, Period.ofDays(1));
  /**
   * A continuous frequency, with a period of zero.
   */
  public static final PeriodFrequency CONTINUOUS = new PeriodFrequency(CONTINUOUS_NAME, Period.ZERO);
  /**
   * A continuous frequency, with a period of zero.
   */
  public static final Map<PeriodFrequency, PeriodFrequency> s_cache = ImmutableMap.<PeriodFrequency, PeriodFrequency> builder().put(ANNUAL, ANNUAL).put(SEMI_ANNUAL, SEMI_ANNUAL).put(QUARTERLY,
      QUARTERLY).put(BIMONTHLY, BIMONTHLY).put(MONTHLY, MONTHLY).put(BIWEEKLY, BIWEEKLY).put(WEEKLY, WEEKLY).put(DAILY, DAILY).put(CONTINUOUS, CONTINUOUS).build();

  /**
   * The name of the convention.
   */
  private final String _name;
  /**
   * The length of the period.
   */
  private final Period _period;

  /**
   * Obtains an instance.
   * 
   * @param name  the name of the convention, not null
   * @param period  length of the period, not null
   * @return a period frequency, not null
   */
  public static PeriodFrequency of(final String name, final Period period) {
    PeriodFrequency temp = new PeriodFrequency(name, period);
    if (s_cache.containsKey(temp)) {
      return s_cache.get(temp);
    }
    return temp;
  }

  /**
   * Creates an instance.
   * 
   * @param name  the name of the convention, not null
   * @param period  length of the period, not null
   */
  /* package */PeriodFrequency(final String name, final Period period) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(period, "period");
    _name = name;
    _period = period;
  }

  // -------------------------------------------------------------------------
  @Override
  public String getConventionName() {
    return _name;
  }

  /**
   * Gets the length of the period defining the convention.
   * 
   * @return the length of the period, not null
   */
  public Period getPeriod() {
    return _period;
  }

  // -------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PeriodFrequency) {
      PeriodFrequency other = (PeriodFrequency) obj;
      return _name.equals(other._name) && _period.equals(other._period);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _name.hashCode() ^ _period.hashCode();
  }

  @Override
  public String toString() {
    return "Frequency[" + "name =  " + _name + " period = " + _period + "]";
  }

  // -------------------------------------------------------------------------
  // REVIEW Elaine 2010-06-18 This is awful, but I'm not sure if we actually need SimpleFrequency,
  // so I'm going to use PeriodFrequency where possible and see if this class can be eliminated entirely
  /**
   * Converts this to a simple frequency.
   * 
   * @return the simple frequency, not null
   */
  public SimpleFrequency toSimpleFrequency() {
    if (_name.equals(ANNUAL_NAME)) {
      return SimpleFrequency.ANNUAL;
    }
    if (_name.equals(BIMONTHLY_NAME)) {
      return SimpleFrequency.BIMONTHLY;
    }
    if (_name.equals(BIWEEKLY_NAME)) {
      return SimpleFrequency.BIWEEKLY;
    }
    if (_name.equals(CONTINUOUS_NAME)) {
      return SimpleFrequency.CONTINUOUS;
    }
    if (_name.equals(DAILY_NAME)) {
      return SimpleFrequency.DAILY;
    }
    if (_name.equals(MONTHLY_NAME)) {
      return SimpleFrequency.MONTHLY;
    }
    if (_name.equals(QUARTERLY_NAME)) {
      return SimpleFrequency.QUARTERLY;
    }
    if (_name.equals(SEMI_ANNUAL_NAME)) {
      return SimpleFrequency.SEMI_ANNUAL;
    }
    if (_name.equals(WEEKLY_NAME)) {
      return SimpleFrequency.WEEKLY;
    }
    throw new IllegalArgumentException("Cannot get a simple frequency for " + toString());
  }

}
