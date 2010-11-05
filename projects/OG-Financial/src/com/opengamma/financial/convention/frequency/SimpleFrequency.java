/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import com.opengamma.util.ArgumentChecker;

/**
 * A simple frequency implementation.
 */
public class SimpleFrequency implements Frequency {
  /**
   * Annual frequency.
   */
  public static final SimpleFrequency ANNUAL = new SimpleFrequency(ANNUAL_NAME, 1);
  /**
   * Semi-annual frequency.
   */
  public static final SimpleFrequency SEMI_ANNUAL = new SimpleFrequency(SEMI_ANNUAL_NAME, 2);
  /**
   * Quarterly frequency.
   */
  public static final SimpleFrequency QUARTERLY = new SimpleFrequency(QUARTERLY_NAME, 4);
  /**
   * Bi-Monthly frequency.
   */
  public static final SimpleFrequency BIMONTHLY = new SimpleFrequency(BIMONTHLY_NAME, 6);
  /**
   * Monthly frequency.
   */
  public static final SimpleFrequency MONTHLY = new SimpleFrequency(MONTHLY_NAME, 12);
  /**
   * Bi-weekly frequency.
   */
  public static final SimpleFrequency BIWEEKLY = new SimpleFrequency(BIWEEKLY_NAME, 26);
  /**
   * weekly frequency.
   */
  public static final SimpleFrequency WEEKLY = new SimpleFrequency(WEEKLY_NAME, 52);
  /**
   * daily frequency.
   */
  public static final SimpleFrequency DAILY = new SimpleFrequency(DAILY_NAME, 365);

  /**
   * continuous frequency.
   */
  //TODO where converting to/from say continuously compounded interest rates, can't use Double.MAX_VALUE, but need different formula 
  public static final SimpleFrequency CONTINUOUS = new SimpleFrequency(CONTINUOUS_NAME, Double.MAX_VALUE);

  /**
   * The convention name.
   */
  private final String _name;
  private final double _periodsPerYear;

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   * @param periodsPerYear the number of periods per year
   * @throws IllegalArgumentException if the name is null
   * @throws IllegalArgumentException if the frequency is zero or negative
   */
  protected SimpleFrequency(final String name, final double periodsPerYear) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNegativeOrZero(periodsPerYear, "periods per year");
    _name = name;
    _periodsPerYear = periodsPerYear;
  }

  @Override
  public String getConventionName() {
    return _name;
  }

  public double getPeriodsPerYear() {
    return _periodsPerYear;
  }

  @Override
  public String toString() {
    return "Frequency[" + "name = " + _name + " pa = " + _periodsPerYear + "]";
  }

  // REVIEW Elaine 2010-06-18 This is awful, but I'm not sure if we actually need SimpleFrequency, 
  // so I'm going to use PeriodFrequency where possible and see if this class can be eliminated entirely 
  public PeriodFrequency toPeriodFrequency() {
    if (_name == ANNUAL_NAME) {
      return PeriodFrequency.ANNUAL;
    }
    if (_name == BIMONTHLY_NAME) {
      return PeriodFrequency.BIMONTHLY;
    }
    if (_name == BIWEEKLY_NAME) {
      return PeriodFrequency.BIWEEKLY;
    }
    if (_name == CONTINUOUS_NAME) {
      return PeriodFrequency.CONTINUOUS;
    }
    if (_name == DAILY_NAME) {
      return PeriodFrequency.DAILY;
    }
    if (_name == MONTHLY_NAME) {
      return PeriodFrequency.MONTHLY;
    }
    if (_name == QUARTERLY_NAME) {
      return PeriodFrequency.QUARTERLY;
    }
    if (_name == SEMI_ANNUAL_NAME) {
      return PeriodFrequency.SEMI_ANNUAL;
    }
    if (_name == WEEKLY_NAME) {
      return PeriodFrequency.WEEKLY;
    }
    throw new IllegalArgumentException("Cannot get a period frequency for " + toString());
  }
}
