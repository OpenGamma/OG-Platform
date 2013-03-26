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

  public static ZonedDateTime getNextIMMDate(final ZonedDateTime date, final Tenor tenor) {
    final ZonedDateTime nextIMMDate = ZonedDateTime.from(IMM_ADJUSTER.adjustInto(date));
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
