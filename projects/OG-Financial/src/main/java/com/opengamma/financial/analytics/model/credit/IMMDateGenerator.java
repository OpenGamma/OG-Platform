/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class IMMDateGenerator {
  private static final NextExpiryAdjuster IMM_ADJUSTER = new NextExpiryAdjuster(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY));

  public static ZonedDateTime getNextIMMDate(final ZonedDateTime date, final Tenor tenor) {
    final ZonedDateTime nextIMMDate = ZonedDateTime.from(IMM_ADJUSTER.adjustInto(date));
    return nextIMMDate.plus(tenor.getPeriod());

  }
}
