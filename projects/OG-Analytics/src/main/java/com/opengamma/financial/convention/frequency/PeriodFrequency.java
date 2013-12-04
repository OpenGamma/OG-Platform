/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.joda.convert.FromString;
import org.threeten.bp.Period;

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
   * A frequency with a period of twenty eight days.
   */
  public static final PeriodFrequency TWENTY_EIGHT_DAYS = new PeriodFrequency(TWENTY_EIGHT_DAYS_NAME, Period.ofDays(28));
  /**
   * A frequency with a period of two weeks.
   */
  public static final PeriodFrequency THREE_WEEKS = new PeriodFrequency(THREE_WEEK_NAME, Period.ofDays(21));
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
   * A frequency with a period of four months
   */
  public static final PeriodFrequency FOUR_MONTHS = new PeriodFrequency(FOUR_MONTH_NAME, Period.ofMonths(4));
  /**
   * A frequency with a period of five months
   */
  public static final PeriodFrequency FIVE_MONTHS = new PeriodFrequency(FIVE_MONTH_NAME, Period.ofMonths(5));
  /**
   * A frequency with a period of seven months
   */
  public static final PeriodFrequency SEVEN_MONTHS = new PeriodFrequency(SEVEN_MONTH_NAME, Period.ofMonths(7));
  /**
   * A frequency with a period of eight months
   */
  public static final PeriodFrequency EIGHT_MONTHS = new PeriodFrequency(EIGHT_MONTH_NAME, Period.ofMonths(8));
  /**
   * A frequency with a period of nine months
   */
  public static final PeriodFrequency NINE_MONTHS = new PeriodFrequency(NINE_MONTH_NAME, Period.ofMonths(9));
  /**
   * A frequency with a period of ten months
   */
  public static final PeriodFrequency TEN_MONTHS = new PeriodFrequency(TEN_MONTH_NAME, Period.ofMonths(10));
  /**
   * A frequency with a period of eleven months
   */
  public static final PeriodFrequency ELEVEN_MONTHS = new PeriodFrequency(ELEVEN_MONTH_NAME, Period.ofMonths(11));
  /**
   * A frequency with a period of eighteen months
   */
  public static final PeriodFrequency EIGHTEEN_MONTHS = new PeriodFrequency(EIGHTEEN_MONTH_NAME, Period.ofMonths(18));
  /**
   * A frequency with a period of never
   */
  public static final PeriodFrequency NEVER = new PeriodFrequency(NEVER_NAME, Period.ZERO);

  /** A map containing all of the frequency */
  public static final Map<PeriodFrequency, PeriodFrequency> s_cache =
      ImmutableMap.<PeriodFrequency, PeriodFrequency>builder()
      .put(ANNUAL, ANNUAL)
      .put(SEMI_ANNUAL, SEMI_ANNUAL)
      .put(QUARTERLY, QUARTERLY)
      .put(BIMONTHLY, BIMONTHLY)
      .put(MONTHLY, MONTHLY)
      .put(BIWEEKLY, BIWEEKLY)
      .put(WEEKLY, WEEKLY)
      .put(DAILY, DAILY)
      .put(CONTINUOUS, CONTINUOUS)
      .put(FOUR_MONTHS, FOUR_MONTHS)
      .put(FIVE_MONTHS, FIVE_MONTHS)
      .put(SEVEN_MONTHS, SEVEN_MONTHS)
      .put(EIGHT_MONTHS, EIGHT_MONTHS)
      .put(NINE_MONTHS, NINE_MONTHS)
      .put(TEN_MONTHS, TEN_MONTHS)
      .put(ELEVEN_MONTHS, ELEVEN_MONTHS)
      .put(EIGHTEEN_MONTHS, EIGHTEEN_MONTHS)
      .build();

  /**
   * The name of the convention.
   */
  private final String _name;
  /**
   * The length of the period.
   */
  private final Period _period;

  /**
   * Gets a frequency from a string.
   * <p>
   * This parses the known {@code PeriodFrequency} instances by name.
   * Name matching is case insensitive.
   * 
   * @param name  the name of the convention, not null
   * @return the period frequency matching the name, not null
   * @throws IllegalArgumentException if the name is unknown
   */
  @FromString
  public static PeriodFrequency of(final String name) {
    String nameLower = name.toLowerCase(Locale.ENGLISH);
    for (PeriodFrequency freq : s_cache.keySet()) {
      if (freq.getName().toLowerCase(Locale.ENGLISH).equals(nameLower)) {
        return freq;
      }
    }
    throw new IllegalArgumentException("Unknown PeriodFrequency: " + name);
  }

  /**
   * Obtains an instance.
   * 
   * @param name  the name of the convention, not null
   * @param period  length of the period, not null
   * @return a period frequency, not null
   */
  public static PeriodFrequency of(final String name, final Period period) {
    final PeriodFrequency temp = new PeriodFrequency(name, period);
    if (s_cache.containsKey(temp)) {
      return s_cache.get(temp);
    }
    return temp;
  }

  /**
   * Constructs a frequency from a period.
   * 
   * @param period The period, not null
   * @return a period frequency, not null
   */
  public static PeriodFrequency of(final Period period) {
    ArgumentChecker.notNull(period, "period");
    for (final Map.Entry<PeriodFrequency, PeriodFrequency> entry : s_cache.entrySet()) {
      if (entry.getKey().getPeriod().normalized().equals(period.normalized())) {
        return entry.getValue();
      }
    }
    return new PeriodFrequency(period.toString(), period);
  }

  /**
   * Given a {@link PeriodFrequency} or {@link SimpleFrequency}, returns a {@link PeriodFrequency}.
   * If the input is already a {@link PeriodFrequency}, then the original object is returned.
   * 
   * @param frequency The frequency, not null
   * @return A frequency based on {@link Period}
   * @throws IllegalArgumentException if the input is not a {@link PeriodFrequency} or {@link SimpleFrequency}
   */
  public static PeriodFrequency convertToPeriodFrequency(final Frequency frequency) {
    ArgumentChecker.notNull(frequency, "frequency");
    if (frequency instanceof PeriodFrequency) {
      return (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      return ((SimpleFrequency) frequency).toPeriodFrequency();
    }
    throw new IllegalArgumentException("Can only handle PeriodFrequency and SimpleFrequency");
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
   * Gets the length of the period defining the convention.
   * 
   * @return the length of the period, not null
   */
  public Period getPeriod() {
    return _period;
  }

  // -------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PeriodFrequency) {
      final PeriodFrequency other = (PeriodFrequency) obj;
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
    if (_name.equals(FOUR_MONTH_NAME)) {
      return SimpleFrequency.FOUR_MONTHS;
    }
    if (_name.equals(FIVE_MONTH_NAME)) {
      return SimpleFrequency.FIVE_MONTHS;
    }
    if (_name.equals(SEVEN_MONTH_NAME)) {
      return SimpleFrequency.SEVEN_MONTHS;
    }
    if (_name.equals(EIGHT_MONTH_NAME)) {
      return SimpleFrequency.EIGHT_MONTHS;
    }
    if (_name.equals(NINE_MONTH_NAME)) {
      return SimpleFrequency.NINE_MONTHS;
    }
    if (_name.equals(TEN_MONTH_NAME)) {
      return SimpleFrequency.TEN_MONTHS;
    }
    if (_name.equals(ELEVEN_MONTH_NAME)) {
      return SimpleFrequency.ELEVEN_MONTHS;
    }
    if (_name.equals(EIGHT_MONTH_NAME)) {
      return SimpleFrequency.EIGHTEEN_MONTHS;
    }
    if (_name.equals(TWENTY_EIGHT_DAYS_NAME)) {
      return SimpleFrequency.TWENTY_EIGHT_DAYS;
    }
    throw new IllegalArgumentException("Cannot get a simple frequency for " + toString());
  }

}
