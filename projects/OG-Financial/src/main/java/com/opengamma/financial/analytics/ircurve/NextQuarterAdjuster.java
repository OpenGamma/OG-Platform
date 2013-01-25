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

/**
 * A {@code TemporalAdjuster} that moves the date to the next March/June/September/December.
 */
public class NextQuarterAdjuster implements TemporalAdjuster {

  /**
   * The expiry months.
   */
  private final Set<Month> _futureQuarters = EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);

  @Override
  public Temporal adjustInto(Temporal temporal) {
    Temporal result = temporal;
    do {
      result = result.plus(1, MONTHS);
    } while (!_futureQuarters.contains(Month.from(result)));
    return result;
  }

}
