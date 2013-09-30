/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.threeten.bp.Duration;
import org.threeten.bp.Period;

import com.opengamma.util.ArgumentChecker;

/**
 * A tenor.
 */
public class Tenor implements Comparable<Tenor>, Serializable {

  /**
   * An overnight tenor.
   */
  public static final Tenor OVERNIGHT = new Tenor(Period.ofDays(1));
  /**
   * A tenor of one day.
   */
  public static final Tenor DAY = new Tenor(Period.ofDays(1));
  /**
   * A tenor of one day.
   */
  public static final Tenor ONE_DAY = new Tenor(Period.ofDays(1));
  /**
   * A tenor of two days.
   */
  public static final Tenor TWO_DAYS = new Tenor(Period.ofDays(2));
  /**
   * A tenor of two days.
   */
  public static final Tenor THREE_DAYS = new Tenor(Period.ofDays(3));
  /**
   * A tenor of 1 week.
   */
  public static final Tenor ONE_WEEK = new Tenor(Period.ofDays(7));
  /**
   * A tenor of 2 weeks.
   */
  public static final Tenor TWO_WEEKS = new Tenor(Period.ofDays(14));
  /**
   * A tenor of 3 weeks.
   */
  public static final Tenor THREE_WEEKS = new Tenor(Period.ofDays(21));
  /**
   * A tenor of 6 weeks.
   */
  public static final Tenor SIX_WEEKS = new Tenor(Period.ofDays(42));
  /**
   * A tenor of 1 month.
   */
  public static final Tenor ONE_MONTH = new Tenor(Period.ofMonths(1));
  /**
   * A tenor of 2 month.
   */
  public static final Tenor TWO_MONTHS = new Tenor(Period.ofMonths(2));
  /**
   * A tenor of 3 month.
   */
  public static final Tenor THREE_MONTHS = new Tenor(Period.ofMonths(3));
  /**
   * A tenor of 4 month.
   */
  public static final Tenor FOUR_MONTHS = new Tenor(Period.ofMonths(4));
  /**
   * A tenor of 5 month.
   */
  public static final Tenor FIVE_MONTHS = new Tenor(Period.ofMonths(5));
  /**
   * A tenor of 6 month.
   */
  public static final Tenor SIX_MONTHS = new Tenor(Period.ofMonths(6));
  /**
   * A tenor of 7 months.
   */
  public static final Tenor SEVEN_MONTHS = new Tenor(Period.ofMonths(7));
  /**
   * A tenor of 8 months.
   */
  public static final Tenor EIGHT_MONTHS = new Tenor(Period.ofMonths(8));
  /**
   * A tenor of 9 month.
   */
  public static final Tenor NINE_MONTHS = new Tenor(Period.ofMonths(9));
  /**
   * A tenor of 10 month.
   */
  public static final Tenor TEN_MONTHS = new Tenor(Period.ofMonths(10));
  /**
   * A tenor of 11 month.
   */
  public static final Tenor ELEVEN_MONTHS = new Tenor(Period.ofMonths(11));
  /**
   * A tenor of 12 months.
   */
  public static final Tenor TWELVE_MONTHS = new Tenor(Period.ofMonths(12));
  /**
   * A tenor of 18 months.
   */
  public static final Tenor EIGHTEEN_MONTHS = new Tenor(Period.ofMonths(18));
  /**
   * A tenor of 1 year.
   */
  public static final Tenor ONE_YEAR = new Tenor(Period.ofYears(1));
  /**
   * A tenor of 2 years.
   */
  public static final Tenor TWO_YEARS = new Tenor(Period.ofYears(2));
  /**
   * A tenor of 3 year.
   */
  public static final Tenor THREE_YEARS = new Tenor(Period.ofYears(3));
  /**
   * A tenor of 4 year.
   */
  public static final Tenor FOUR_YEARS = new Tenor(Period.ofYears(4));
  /**
   * A tenor of 5 years.
   */
  public static final Tenor FIVE_YEARS = new Tenor(Period.ofYears(5));
  /**
   * A tenor of 6 years.
   */
  public static final Tenor SIX_YEARS = new Tenor(Period.ofYears(6));
  /**
   * A tenor of 7 years.
   */
  public static final Tenor SEVEN_YEARS = new Tenor(Period.ofYears(7));
  /**
   * A tenor of 8 years.
   */
  public static final Tenor EIGHT_YEARS = new Tenor(Period.ofYears(8));
  /**
   * A tenor of 9 years.
   */
  public static final Tenor NINE_YEARS = new Tenor(Period.ofYears(9));
  /**
   * A tenor of 10 years.
   */
  public static final Tenor TEN_YEARS = new Tenor(Period.ofYears(10));
  /**
   * A tenor of one working week (5 days).
   */
  public static final Tenor WORKING_WEEK = new Tenor(Period.ofDays(5));
//  /**
//   * A tenor of the working days in a year measured in hours (250 * 24 hours).
//   */
//  public static final Tenor WORKING_DAYS_IN_YEAR = new Tenor(Period.of(252 * 24, HOURS)); // TODO: should be days???
//  /**
//   * A tenor of the working days in a month measured in hours (250 * 24 / 12 hours).
//   */
//  public static final Tenor WORKING_DAYS_IN_MONTH = new Tenor(Period.of(WORKING_DAYS_IN_YEAR.getPeriod().toDuration().dividedBy(12)));
//  /**
//   * A tenor of one financial year measured in hours (365.25 * 24 hours).
//   */
//  public static final Tenor FINANCIAL_YEAR = new Tenor(Period.of((int) (365.25 * 24), HOURS));
  /**
   * A tenor of the days in a standard year (365 days).
   */
  public static final Tenor YEAR = new Tenor(Period.ofDays(365));
  /**
   * A tenor of the days in a leap year (366 days).
   */
  public static final Tenor LEAP_YEAR = new Tenor(Period.ofDays(366));
//  /**
//   * A tenor of two financial years measured in hours (365.25 * 24 * 2 hours).
//   */
//  public static final Tenor TWO_FINANCIAL_YEARS = new Tenor(FINANCIAL_YEAR.getPeriod().multipliedBy(2));

  /**
   * The period of the tenor.
   */
  private final Period _period;

  //-------------------------------------------------------------------------
  /**
   * Returns a formatted string representing the tenor.
   * <p>
   * The format is based on ISO-8601, such as 'P3M'.
   * 
   * @param period  the period to convert to a tenor, not null
   * @return the formatted tenor, not null
   */
  public static Tenor of(Period period) {
    ArgumentChecker.notNull(period, "period");
    return new Tenor(period);
  }

  /**
   * Parses a formatted string representing the tenor.
   * <p>
   * The format is based on ISO-8601, such as 'P3M'.
   * 
   * @param tenorStr  the string representing the tenor, not null
   * @return the tenor, not null
   */
  @FromString
  public static Tenor parse(String tenorStr) {
    ArgumentChecker.notNull(tenorStr, "tenorStr");
    return new Tenor(Period.parse(tenorStr));
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a tenor.
   * @param period  the period to represent
   * @deprecated Use the static factory method {@code Tenor.of(Period)}.
   */
  @Deprecated
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

  public static final Tenor ofDays(int days) {
    return new Tenor(Period.ofDays(days));
  }

  public static final Tenor ofWeeks(int weeks) {
    return new Tenor(Period.ofDays(weeks * 7));
  }

  public static final Tenor ofMonths(int months) {
    return new Tenor(Period.ofMonths(months)); // TODO: what do we do here
  }

  public static final Tenor ofYears(int years) {
    return new Tenor(Period.ofYears(years)); // TODO: what do we do here
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a formatted string representing the tenor.
   * <p>
   * The format is based on ISO-8601, such as 'P3M'.
   * 
   * @return the formatted tenor, not null
   */
  @ToString
  public String toFormattedString() {
    return getPeriod().toString();
  }

  //-------------------------------------------------------------------------
  //TODO [PLAT-1013] not the best way to do this
  @Override
  public int compareTo(Tenor other) {
    Duration thisDur = DateUtils.estimatedDuration(this._period);
    Duration otherDur = DateUtils.estimatedDuration(other._period);
    return thisDur.compareTo(otherDur);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Tenor)) {
      return false;
    }
    Tenor other = (Tenor) o;
    return getPeriod().equals(other.getPeriod());
  }

  @Override
  public int hashCode() {
    return getPeriod().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Tenor[");
    sb.append(getPeriod().toString());
    sb.append("]");
    return sb.toString();
  }

}
