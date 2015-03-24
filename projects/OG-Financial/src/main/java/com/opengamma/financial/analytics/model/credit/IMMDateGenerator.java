/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class IMMDateGenerator {
  private static final NextExpiryAdjuster IMM_ADJUSTER = new NextExpiryAdjuster(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY));
  private static final Set<Month> IMM_MONTHS = ImmutableSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);
  private static final int TWENTIETH = 20;

  public static ZonedDateTime getNextIMMDate(final ZonedDateTime date, final Tenor tenor) {
    // If 19th of month (IMM date - 1 day) we need to cycle to next IMM period, as effective date of trade on date t is t + 1
    final ZonedDateTime dateWithTradeAdjustment = ((isIMMDate(date) && date.getDayOfMonth() == TWENTIETH - 1)) ? ZonedDateTime.from(date).plusDays(1) : ZonedDateTime.from(date);
    final ZonedDateTime nextIMMDate = ZonedDateTime.from(IMM_ADJUSTER.adjustInto(dateWithTradeAdjustment)).withDayOfMonth(TWENTIETH); // must be 20th
    return nextIMMDate.plus(tenor.getPeriod());
  }

  public static ZonedDateTime getNextIMMDateNoAdjustment(final ZonedDateTime date, final Tenor tenor) {
    ZonedDateTime nextIMMDateTime;
    if (IMMDateLogic.isIMMDate(date.toLocalDate())) {
      nextIMMDateTime = date;
    } else {
      LocalDate nextIMMDate =  IMMDateLogic.getNextIMMDate(date.toLocalDate());
      nextIMMDateTime = date.with(nextIMMDate);
    }
    return nextIMMDateTime.plus(tenor.getPeriod());
  }
  
  
  public static LocalDate getPreviousIMMDate(final LocalDate date) {
    final TemporalAdjuster adjuster = new TemporalAdjuster() {
      @Override
      public Temporal adjustInto(final Temporal temporal) {
        Temporal adjusted = temporal;
        do {
          adjusted = adjusted.minus(1, ChronoUnit.MONTHS);
        } while (!IMM_MONTHS.contains(Month.from(adjusted)));
        return adjusted;
      }
    };
    return date.with(adjuster);
  }

  public static LegacyVanillaCreditDefaultSwapDefinition cdsModifiedForIMM(final ZonedDateTime now, LegacyVanillaCreditDefaultSwapDefinition cds) {
    if (!isIMMDate(cds.getMaturityDate())) {
      cds = cds.withStartDate(now);
    }
    return cds;
  }

  public static boolean isIMMDate(final ZonedDateTime date) {
    return IMM_MONTHS.contains(date.getMonth());
  }
}
