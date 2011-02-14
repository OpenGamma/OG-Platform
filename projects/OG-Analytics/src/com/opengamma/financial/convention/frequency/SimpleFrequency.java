/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of the frequency convention.
 */
public final class SimpleFrequency implements Frequency, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * A frequency with a period of one year.
   */
  public static final SimpleFrequency NEVER = new SimpleFrequency(NEVER_NAME, 0);
  /**
   * A frequency with a period of one year.
   */
  public static final SimpleFrequency ANNUAL = new SimpleFrequency(ANNUAL_NAME, 1);
  /**
   * A frequency with a period of six months.
   */
  public static final SimpleFrequency SEMI_ANNUAL = new SimpleFrequency(SEMI_ANNUAL_NAME, 2);
  /**
   * A frequency with a period of three months.
   */
  public static final SimpleFrequency QUARTERLY = new SimpleFrequency(QUARTERLY_NAME, 4);
  /**
   * A frequency with a period of two months.
   */
  public static final SimpleFrequency BIMONTHLY = new SimpleFrequency(BIMONTHLY_NAME, 6);
  /**
   * A frequency with a period of one month.
   */
  public static final SimpleFrequency MONTHLY = new SimpleFrequency(MONTHLY_NAME, 12);
  /**
   * A frequency with a period of two weeks.
   */
  public static final SimpleFrequency BIWEEKLY = new SimpleFrequency(BIWEEKLY_NAME, 26);
  /**
   * A frequency with a period of one week.
   */
  public static final SimpleFrequency WEEKLY = new SimpleFrequency(WEEKLY_NAME, 52);
  /**
   * A frequency with a period of one day.
   */
  public static final SimpleFrequency DAILY = new SimpleFrequency(DAILY_NAME, 365);
  /**
   * A continuous frequency.
   */
  // TODO where converting to/from say continuously compounded interest rates, can't use Double.MAX_VALUE, but need different formula
  public static final SimpleFrequency CONTINUOUS = new SimpleFrequency(CONTINUOUS_NAME, Double.MAX_VALUE);

  /**
   * The name of the convention.
   */
  private final String _name;
  /**
   * The number of periods per year.
   */
  private final Double _periodsPerYear;

  /**
   * Creates an instance.
   * 
   * @param name  the convention name, not null
   * @param periodsPerYear  the number of periods per year, greater than zero
   * @throws IllegalArgumentException if the name is null
   * @throws IllegalArgumentException if the frequency is zero or negative
   */
  /* package */SimpleFrequency(final String name, final double periodsPerYear) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNegative(periodsPerYear, "periods per year");
    _name = name;
    _periodsPerYear = periodsPerYear;
  }

  // -------------------------------------------------------------------------
  @Override
  public String getConventionName() {
    return _name;
  }

  /**
   * Gets the number of periods per year.
   * 
   * @return the periods per year
   */
  public double getPeriodsPerYear() {
    return _periodsPerYear;
  }

  // -------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SimpleFrequency) {
      SimpleFrequency other = (SimpleFrequency) obj;
      return _name.equals(other._name) && _periodsPerYear.equals(other._periodsPerYear);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _name.hashCode() ^ _periodsPerYear.hashCode();
  }

  @Override
  public String toString() {
    return "Frequency[" + "name = " + _name + " pa = " + _periodsPerYear + "]";
  }

  // -------------------------------------------------------------------------
  // REVIEW Elaine 2010-06-18 This is awful, but I'm not sure if we actually need SimpleFrequency,
  // so I'm going to use PeriodFrequency where possible and see if this class can be eliminated entirely
  /**
   * Converts this to a period frequency.
   * 
   * @return the period frequency, not null
   */
  public PeriodFrequency toPeriodFrequency() {
    if (_name.equals(ANNUAL_NAME)) {
      return PeriodFrequency.ANNUAL;
    }
    if (_name.equals(BIMONTHLY_NAME)) {
      return PeriodFrequency.BIMONTHLY;
    }
    if (_name.equals(BIWEEKLY_NAME)) {
      return PeriodFrequency.BIWEEKLY;
    }
    if (_name.equals(CONTINUOUS_NAME)) {
      return PeriodFrequency.CONTINUOUS;
    }
    if (_name.equals(DAILY_NAME)) {
      return PeriodFrequency.DAILY;
    }
    if (_name.equals(MONTHLY_NAME)) {
      return PeriodFrequency.MONTHLY;
    }
    if (_name.equals(QUARTERLY_NAME)) {
      return PeriodFrequency.QUARTERLY;
    }
    if (_name.equals(SEMI_ANNUAL_NAME)) {
      return PeriodFrequency.SEMI_ANNUAL;
    }
    if (_name.equals(WEEKLY_NAME)) {
      return PeriodFrequency.WEEKLY;
    }
    throw new IllegalArgumentException("Cannot get a period frequency for " + toString());
  }

}
