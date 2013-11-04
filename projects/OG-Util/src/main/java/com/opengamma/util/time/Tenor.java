/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

import java.io.Serializable;

import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.threeten.bp.Duration;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.util.ArgumentChecker;

/**
 * A tenor.
 */
public class Tenor implements Comparable<Tenor>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = -6312355131513714559L;

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
   * A tenor of 2 months.
   */
  public static final Tenor TWO_MONTHS = new Tenor(Period.ofMonths(2));
  /**
   * A tenor of 3 months.
   */
  public static final Tenor THREE_MONTHS = new Tenor(Period.ofMonths(3));
  /**
   * A tenor of 4 months.
   */
  public static final Tenor FOUR_MONTHS = new Tenor(Period.ofMonths(4));
  /**
   * A tenor of 5 months.
   */
  public static final Tenor FIVE_MONTHS = new Tenor(Period.ofMonths(5));
  /**
   * A tenor of 6 months.
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
   * A tenor of 9 months.
   */
  public static final Tenor NINE_MONTHS = new Tenor(Period.ofMonths(9));
  /**
   * A tenor of 10 months.
   */
  public static final Tenor TEN_MONTHS = new Tenor(Period.ofMonths(10));
  /**
   * A tenor of 11 months.
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
   * A tenor of 3 years.
   */
  public static final Tenor THREE_YEARS = new Tenor(Period.ofYears(3));
  /**
   * A tenor of 4 years.
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
  /**
   * A tenor of the days in a standard year (365 days).
   */
  public static final Tenor YEAR = new Tenor(Period.ofDays(365));
  /**
   * A tenor of the days in a leap year (366 days).
   */
  public static final Tenor LEAP_YEAR = new Tenor(Period.ofDays(366));
  /**
   * An overnight / next (O/N) tenor.
   */
  public static final Tenor ON = new Tenor(BusinessDayTenor.OVERNIGHT);
  /**
   * A spot / next (S/N) tenor.
   */
  public static final Tenor SN = new Tenor(BusinessDayTenor.SPOT_NEXT);
  /**
   * A tomorrow / next (a.k.a. tom next, T/N) tenor.
   */
  public static final Tenor TN = new Tenor(BusinessDayTenor.TOM_NEXT);
  
  public enum BusinessDayTenor {
    OVERNIGHT("ON", Period.ofDays(1)),
    SPOT_NEXT("SN", Period.ofDays(2)),
    TOM_NEXT("TN", Period.ofDays(3));
    
    private final String _shortName;
    private final Duration _approximateDuration;
    
    private BusinessDayTenor(final String shortName, final Period approximateDuration) {
      _shortName = shortName;
      _approximateDuration = DAYS.getDuration().multipliedBy(approximateDuration.getDays());
    }
    
    public String toString() {
      return _shortName;
    }
    
    public Duration getApproximateDuration() {
      return _approximateDuration;
    }
  }
  
  /**
   * The period of the tenor.
   */
  private final Period _period;
  /**
   * The business day tenor.
   */
  private final BusinessDayTenor _businessDayTenor;

  //-------------------------------------------------------------------------
  /**
   * Returns a formatted string representing the tenor.
   * <p>
   * The format is based on ISO-8601, such as 'P3M'.
   * 
   * @param period  the period to convert to a tenor, not null
   * @return the formatted tenor, not null
   */
  public static Tenor of(final Period period) {
    ArgumentChecker.notNull(period, "period");
    return new Tenor(period);
  }

  public static Tenor of(final BusinessDayTenor businessDayTenor) {
    ArgumentChecker.notNull(businessDayTenor, "business day tenor");
    return new Tenor(businessDayTenor);
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
  public static Tenor parse(final String tenorStr) {
    ArgumentChecker.notNull(tenorStr, "tenorStr");
    try {
      return new Tenor(Period.parse(tenorStr));
    } catch (DateTimeParseException e) {
      return new Tenor(BusinessDayTenor.valueOf(tenorStr));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a tenor.
   * @param period  the period to represent
   * @deprecated Use the static factory method {@code Tenor.of(Period)}.
   */
  @Deprecated
  public Tenor(final Period period) {
    ArgumentChecker.notNull(period, "period"); //change of behaviour
    _period = period;
    _businessDayTenor = null;
  }

  /**
   * Creates a tenor without a period. This is used for overnight,
   * spot next and tomorrow next tenors.
   */
  private Tenor(final BusinessDayTenor businessDayTenor) {
    ArgumentChecker.notNull(businessDayTenor, "business day tenor");
    _period = null;
    _businessDayTenor = businessDayTenor;
  }
  
  /**
   * Gets the tenor period.
   * @return the period
   */
  public Period getPeriod() {
    if (_period == null) {
      throw new IllegalStateException("Could not get period for " + toString());
    }
    return _period;
  }

  public BusinessDayTenor getBusinessDayTenor() {
    if (_businessDayTenor == null) {
      throw new IllegalStateException("Could not get business day tenor for " + toString());
    }
    return _businessDayTenor;
  }
  
  public static final Tenor ofDays(final int days) {
    return new Tenor(Period.ofDays(days));
  }

  public static final Tenor ofWeeks(final int weeks) {
    return new Tenor(Period.ofDays(weeks * 7));
  }

  public static final Tenor ofMonths(final int months) {
    return new Tenor(Period.ofMonths(months)); // TODO: what do we do here
  }

  public static final Tenor ofYears(final int years) {
    return new Tenor(Period.ofYears(years)); // TODO: what do we do here
  }

  public static final Tenor ofBusinessDay(final BusinessDayTenor businessDayTenor) {
    return new Tenor(businessDayTenor);
  }
  
  public static final Tenor ofBusinessDay(final String businessDayTenor) {
    return new Tenor(BusinessDayTenor.valueOf(businessDayTenor));
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
    if (_period != null) {
      return getPeriod().toString();
    } 
    return getBusinessDayTenor().toString();
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(final Tenor other) {
    final Duration thisDur, otherDur;    
    if (_period == null) {
      thisDur = _businessDayTenor.getApproximateDuration();
    } else {
      thisDur = DateUtils.estimatedDuration(_period);
    }
    if (other._period == null) {
      otherDur = _businessDayTenor.getApproximateDuration();
    } else {
      otherDur = DateUtils.estimatedDuration(_period);
    }
    return thisDur.compareTo(otherDur);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Tenor)) {
      return false;
    }
    Tenor other = (Tenor) o;
    if (_period == null) {
      return _businessDayTenor == other.getBusinessDayTenor();
    }
    return getPeriod().equals(other.getPeriod());
  }

  @Override
  public int hashCode() {
    if (_period == null) {
      return getBusinessDayTenor().hashCode();
    }
    return getPeriod().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Tenor[");
    if (_period == null) {
      sb.append(getBusinessDayTenor().toString());
    } else {
      sb.append(getPeriod().toString());
    }
    sb.append("]");
    return sb.toString();
  }

}
