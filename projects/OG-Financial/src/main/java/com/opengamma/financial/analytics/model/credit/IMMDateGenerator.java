/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class IMMDateGenerator {
  private static final NextExpiryAdjuster IMM_ADJUSTER = new NextExpiryAdjuster(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY));
  private static Set<Month> s_imm_MONTHS = ImmutableSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);
  private static final int s_TWENTIETH = 20;

  public static ZonedDateTime getNextIMMDate(final ZonedDateTime date, final Tenor tenor) {
    // If 19th of month (IMM date - 1 day) we need to cycle to next IMM period, as effective date of trade on date t is t + 1
    final ZonedDateTime DateWithTradeAdjustment = ((isIMMDate(date) && date.getDayOfMonth() == s_TWENTIETH - 1)) ? ZonedDateTime.from(date).plusDays(1) : ZonedDateTime.from(date);
    final ZonedDateTime nextIMMDate = ZonedDateTime.from(IMM_ADJUSTER.adjustInto(DateWithTradeAdjustment)).withDayOfMonth(s_TWENTIETH); // must be 20th
    return nextIMMDate.plus(tenor.getPeriod());
  }

  public static LegacyVanillaCreditDefaultSwapDefinition cdsModifiedForIMM(final ZonedDateTime now, LegacyVanillaCreditDefaultSwapDefinition cds) {
    if (!isIMMDate(cds.getMaturityDate())) {
      cds = cds.withStartDate(now);
    }
    return cds;
  }

  public static boolean isIMMDate(final ZonedDateTime date) {
    return s_imm_MONTHS.contains(date.getMonth());
  }
}
