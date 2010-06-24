/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.Period;

/**
 * A tenor.
 */
public class Tenor {

  /**
   * A tenor of one day.
   */
  public static final Tenor DAY = new Tenor(Period.ofDays(1));
  /**
   * A tenor of one working week (5 days).
   */
  public static final Tenor WORKING_WEEK = new Tenor(Period.ofDays(5));
  /**
   * A tenor of the working days in a year measured in hours (250 * 24 hours).
   */
  public static final Tenor WORKING_DAYS_IN_YEAR = new Tenor(Period.ofHours(250 * 24));  // TODO: should be days???
  /**
   * A tenor of the working days in a month measured in hours (250 * 24 / 12 hours).
   */
  public static final Tenor WORKING_DAYS_IN_MONTH = new Tenor(WORKING_DAYS_IN_YEAR.getPeriod().dividedBy(12));
  /**
   * A tenor of one financial year measured in hours (365.25 * 24 hours).
   */
  public static final Tenor FINANCIAL_YEAR = new Tenor(Period.ofHours((int) (365.25 * 24)));
  /**
   * A tenor of the days in a standard year (365 days).
   */
  public static final Tenor YEAR = new Tenor(Period.ofDays(365));
  /**
   * A tenor of the days in a leap year (366 days).
   */
  public static final Tenor LEAP_YEAR = new Tenor(Period.ofDays(366));
  /**
   * A tenor of two financial years measured in hours (365.25 * 24 * 2 hours).
   */
  public static final Tenor TWO_FINANCIAL_YEARS = new Tenor(FINANCIAL_YEAR.getPeriod().multipliedBy(2));
  /**
   * A tenor of the days in a standard year (365 / 12 days).
   */
  public static final Tenor MONTH = new Tenor(YEAR.getPeriod().dividedBy(12));

  /**
   * The period of the tenor.
   */
  private final Period _period;

  /**
   * Creates a tenor.
   * @param period  the period to represent
   */
  public Tenor(final Period period) {
    _period = period;
  }

  /**
   * Gets the tenor period.
   * @return the period
   */
  public Period getPeriod() {
    return _period;
  }

}
