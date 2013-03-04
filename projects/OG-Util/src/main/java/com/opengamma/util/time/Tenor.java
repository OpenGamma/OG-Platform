/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;
import static org.threeten.bp.temporal.ChronoUnit.HOURS;
import static org.threeten.bp.temporal.ChronoUnit.MONTHS;
import static org.threeten.bp.temporal.ChronoUnit.YEARS;

import org.threeten.bp.Duration;
import org.threeten.bp.Period;

/**
 * A tenor.
 */
public class Tenor implements Comparable<Tenor> {

  /**
   * An overnight tenor.
   */
  public static final Tenor OVERNIGHT = new Tenor(Period.of(1, DAYS));
  /**
   * A tenor of one day.
   */
  public static final Tenor DAY = new Tenor(Period.of(1, DAYS));
  /**
   * A tenor of one day.
   */
  public static final Tenor ONE_DAY = new Tenor(Period.of(1, DAYS));
  /**
   * A tenor of two days.
   */
  public static final Tenor TWO_DAYS = new Tenor(Period.of(2, DAYS));
  /**
   * A tenor of two days.
   */
  public static final Tenor THREE_DAYS = new Tenor(Period.of(3, DAYS));
  /**
   * A tenor of 1 week.
   */
  public static final Tenor ONE_WEEK = new Tenor(Period.of(7, DAYS));
  /**
   * A tenor of 2 weeks.
   */
  public static final Tenor TWO_WEEKS = new Tenor(Period.of(14, DAYS));
  /**
   * A tenor of 3 weeks.
   */
  public static final Tenor THREE_WEEKS = new Tenor(Period.of(21, DAYS));
  /**
   * A tenor of 6 weeks.
   */
  public static final Tenor SIX_WEEKS = new Tenor(Period.of(42, DAYS));
  /**
   * A tenor of 1 month.
   */
  public static final Tenor ONE_MONTH = new Tenor(Period.of(1, MONTHS));
  /**
   * A tenor of 2 month.
   */
  public static final Tenor TWO_MONTHS = new Tenor(Period.of(2, MONTHS));
  /**
   * A tenor of 3 month.
   */
  public static final Tenor THREE_MONTHS = new Tenor(Period.of(3, MONTHS));
  /**
   * A tenor of 4 month.
   */
  public static final Tenor FOUR_MONTHS = new Tenor(Period.of(4, MONTHS));
  /**
   * A tenor of 5 month.
   */
  public static final Tenor FIVE_MONTHS = new Tenor(Period.of(5, MONTHS));
  /**
   * A tenor of 6 month.
   */
  public static final Tenor SIX_MONTHS = new Tenor(Period.of(6, MONTHS));
  /**
   * A tenor of 7 months.
   */
  public static final Tenor SEVEN_MONTHS = new Tenor(Period.of(7, MONTHS));
  /**
   * A tenor of 8 months.
   */
  public static final Tenor EIGHT_MONTHS = new Tenor(Period.of(8, MONTHS));
  /**
   * A tenor of 9 month.
   */
  public static final Tenor NINE_MONTHS = new Tenor(Period.of(9, MONTHS));
  /**
   * A tenor of 10 month.
   */
  public static final Tenor TEN_MONTHS = new Tenor(Period.of(10, MONTHS));
  /**
   * A tenor of 11 month.
   */
  public static final Tenor ELEVEN_MONTHS = new Tenor(Period.of(11, MONTHS));
  /**
   * A tenor of 12 months.
   */
  public static final Tenor TWELVE_MONTHS = new Tenor(Period.of(12, MONTHS));
  /**
   * A tenor of 18 months.
   */
  public static final Tenor EIGHTEEN_MONTHS = new Tenor(Period.of(18, MONTHS));
  /**
   * A tenor of 1 year.
   */
  public static final Tenor ONE_YEAR = new Tenor(Period.of(1, YEARS));
  /**
   * A tenor of 2 years.
   */
  public static final Tenor TWO_YEARS = new Tenor(Period.of(2, YEARS));
  /**
   * A tenor of 3 year.
   */
  public static final Tenor THREE_YEARS = new Tenor(Period.of(3, YEARS));
  /**
   * A tenor of 4 year.
   */
  public static final Tenor FOUR_YEARS = new Tenor(Period.of(4, YEARS));
  /**
   * A tenor of 5 years.
   */
  public static final Tenor FIVE_YEARS = new Tenor(Period.of(5, YEARS));
  /**
   * A tenor of 6 years.
   */
  public static final Tenor SIX_YEARS = new Tenor(Period.of(6, YEARS));
  /**
   * A tenor of 7 years.
   */
  public static final Tenor SEVEN_YEARS = new Tenor(Period.of(7, YEARS));
  /**
   * A tenor of 8 years.
   */
  public static final Tenor EIGHT_YEARS = new Tenor(Period.of(8, YEARS));
  /**
   * A tenor of 9 years.
   */
  public static final Tenor NINE_YEARS = new Tenor(Period.of(9, YEARS));
  /**
   * A tenor of 10 years.
   */
  public static final Tenor TEN_YEARS = new Tenor(Period.of(10, YEARS));
  /**
   * A tenor of one working week (5 days).
   */
  public static final Tenor WORKING_WEEK = new Tenor(Period.of(5, DAYS));
  /**
   * A tenor of the working days in a year measured in hours (250 * 24 hours).
   */
  public static final Tenor WORKING_DAYS_IN_YEAR = new Tenor(Period.of(252 * 24, HOURS)); // TODO: should be days???
  /**
   * A tenor of the working days in a month measured in hours (250 * 24 / 12 hours).
   */
  public static final Tenor WORKING_DAYS_IN_MONTH = new Tenor(Period.of(WORKING_DAYS_IN_YEAR.getPeriod().toDuration().dividedBy(12)));
  /**
   * A tenor of one financial year measured in hours (365.25 * 24 hours).
   */
  public static final Tenor FINANCIAL_YEAR = new Tenor(Period.of((int) (365.25 * 24), HOURS));
  /**
   * A tenor of the days in a standard year (365 days).
   */
  public static final Tenor YEAR = new Tenor(Period.of(365, DAYS));
  /**
   * A tenor of the days in a leap year (366 days).
   */
  public static final Tenor LEAP_YEAR = new Tenor(Period.of(366, DAYS));
  /**
   * A tenor of two financial years measured in hours (365.25 * 24 * 2 hours).
   */
  public static final Tenor TWO_FINANCIAL_YEARS = new Tenor(FINANCIAL_YEAR.getPeriod().multipliedBy(2));

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

  public static final Tenor ofDays(int days) {
    return new Tenor(Period.of(days, DAYS));
  }

  public static final Tenor ofWeeks(int weeks) {
    return new Tenor(Period.of(weeks * 7, DAYS));
  }

  public static final Tenor ofMonths(int months) {
    return new Tenor(Period.of(months, MONTHS)); // TODO: what do we do here
  }

  public static final Tenor ofYears(int years) {
    return new Tenor(Period.of(years, YEARS)); // TODO: what do we do here
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

  //TODO [PLAT-1013] not the best way to do this
  @Override
  public int compareTo(Tenor other) {
    Duration thisDur = DateUtils.estimatedDuration(this._period);
    Duration otherDur = DateUtils.estimatedDuration(other._period);
    return thisDur.compareTo(otherDur);
  }
}
