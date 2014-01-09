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
   * A frequency with a period of zero.
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
   * A frequency with a period of twenty eight days.
   */
  public static final SimpleFrequency TWENTY_EIGHT_DAYS = new SimpleFrequency(TWENTY_EIGHT_DAYS_NAME, 365. / 28);
  /**
   * A frequency with a period of three weeks.
   */
  public static final SimpleFrequency THREE_WEEKS = new SimpleFrequency(THREE_WEEK_NAME, 365. / 21);
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
   * A frequency with a period of four months
   */
  public static final SimpleFrequency FOUR_MONTHS = new SimpleFrequency(FOUR_MONTH_NAME, 3);
  /**
   * A frequency with a period of five months
   */
  public static final SimpleFrequency FIVE_MONTHS = new SimpleFrequency(FIVE_MONTH_NAME, 2.4);
  /**
   * A frequency with a period of seven months
   */
  public static final SimpleFrequency SEVEN_MONTHS = new SimpleFrequency(SEVEN_MONTH_NAME, 12. / 7);
  /**
   * A frequency with a period of eight months
   */
  public static final SimpleFrequency EIGHT_MONTHS = new SimpleFrequency(EIGHT_MONTH_NAME, 1.5);
  /**
   * A frequency with a period of nine months
   */
  public static final SimpleFrequency NINE_MONTHS = new SimpleFrequency(NINE_MONTH_NAME, 4. / 3);
  /**
   * A frequency with a period of ten months
   */
  public static final SimpleFrequency TEN_MONTHS = new SimpleFrequency(TEN_MONTH_NAME, 1.2);
  /**
   * A frequency with a period of eleven months
   */
  public static final SimpleFrequency ELEVEN_MONTHS = new SimpleFrequency(ELEVEN_MONTH_NAME, 12. / 11);
  /**
   * A frequency with a period of eighteen months
   */
  public static final SimpleFrequency EIGHTEEN_MONTHS = new SimpleFrequency(EIGHTEEN_MONTH_NAME, 12. / 18);

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
  /**
   * @deprecated use getName()
   * @return the name of the convention
   */
  @Override
  @Deprecated
  public String getConventionName() {
    return getName();
  }

  @Override
  public String getName() {
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
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SimpleFrequency) {
      final SimpleFrequency other = (SimpleFrequency) obj;
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
    if (_name.equals(FOUR_MONTH_NAME)) {
      return PeriodFrequency.FOUR_MONTHS;
    }
    if (_name.equals(FIVE_MONTH_NAME)) {
      return PeriodFrequency.FIVE_MONTHS;
    }
    if (_name.equals(SEVEN_MONTH_NAME)) {
      return PeriodFrequency.SEVEN_MONTHS;
    }
    if (_name.equals(EIGHT_MONTH_NAME)) {
      return PeriodFrequency.EIGHT_MONTHS;
    }
    if (_name.equals(NINE_MONTH_NAME)) {
      return PeriodFrequency.NINE_MONTHS;
    }
    if (_name.equals(TEN_MONTH_NAME)) {
      return PeriodFrequency.TEN_MONTHS;
    }
    if (_name.equals(ELEVEN_MONTH_NAME)) {
      return PeriodFrequency.ELEVEN_MONTHS;
    }
    if (_name.equals(EIGHTEEN_MONTH_NAME)) {
      return PeriodFrequency.EIGHTEEN_MONTHS;
    }
    if (_name.equals(TWENTY_EIGHT_DAYS_NAME)) {
      return PeriodFrequency.TWENTY_EIGHT_DAYS;
    }
    if (_name.equals(THREE_WEEK_NAME)) {
      return PeriodFrequency.THREE_WEEKS;
    }
    if (_name.equals(NEVER_NAME)) {
      return PeriodFrequency.NEVER;
    }
    throw new IllegalArgumentException("Cannot get a period frequency for " + toString());
  }

}
