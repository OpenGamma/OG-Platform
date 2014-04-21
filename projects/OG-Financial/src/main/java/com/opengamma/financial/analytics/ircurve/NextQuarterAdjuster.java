/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

import java.util.EnumSet;
import java.util.Set;

import org.threeten.bp.Month;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.util.ArgumentChecker;

/**
 * A {@code TemporalAdjuster} that moves the date to the next Quarter within a cycle of four.<p>
 * If the default constructor is used, this will be March/June/September/December.
 */
public class NextQuarterAdjuster implements TemporalAdjuster {

  /** Default constructor uses the March Quarterly cycle: March, June, September, December */
  public NextQuarterAdjuster() {
    _futureQuarters = EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);
  }
  
  /** @param futureQuarters a set of {@link Month}'s, eg EnumSet.of(Month.FEBRUARY, Month.MAY, Month.AUGUST, Month.NOVEMBER) */
  public NextQuarterAdjuster(final Set<Month> futureQuarters) {
    ArgumentChecker.notNull(futureQuarters, "futureQuarters");
    _futureQuarters = futureQuarters;
  }
  
  /** @param month a Month from which a set of 4 will be created, each 3 months apart */
  public NextQuarterAdjuster(final Month month) {
    ArgumentChecker.notNull(month, "month");
    _futureQuarters = EnumSet.of(month, month.plus(3), month.plus(6), month.plus(9));
  }
  
  
  /** The expiry months */
  private final Set<Month> _futureQuarters;

  @Override
  public Temporal adjustInto(Temporal temporal) {
    Temporal result = temporal;
    do {
      result = result.plus(1, MONTHS);
    } while (!_futureQuarters.contains(Month.from(result)));
    return result;
  }

  /** @return the Set of expiry Months' */
  public Set<Month> getFutureQuarters() {
    return _futureQuarters;
  }
  
  

}
