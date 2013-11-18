/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.time.Tenor.BusinessDayTenor;

/**
 * Utility class for {@link Tenor}s.
 */
public class TenorUtils {

  /**
   * Adjusts a {@link ZonedDateTime} by a tenor that is backed by a {@link Period}.
   * @param date The date to adjust, not null
   * @param tenor The tenor, not null
   * @return The date adjusted by a tenor
   * @throws IllegalStateException If the tenor is not backed by a {@link Period}
   */
  public static ZonedDateTime adjustDateByTenor(final ZonedDateTime date, final Tenor tenor) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.isTrue(!tenor.isBusinessDayTenor(), "Business day tenors must have a calendar and initial offset to adjust a date");
    return date.plus(tenor.getPeriod());
  }

  /**
   * Adjusts a {@link LocalDateTime} by a tenor that is backed by a {@link Period}.
   * @param date The date to adjust, not null
   * @param tenor The tenor, not null
   * @return The date adjusted by a tenor
   * @throws IllegalStateException If the tenor is not backed by a {@link Period}
   */
  public static LocalDateTime adjustDateByTenor(final LocalDateTime date, final Tenor tenor) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.isTrue(!tenor.isBusinessDayTenor(), "Business day tenors must have a calendar and initial offset to adjust a date");
    return date.plus(tenor.getPeriod());
  }

  /**
   * Adjusts a {@link LocalDate} by a tenor that is backed by a {@link Period}.
   * @param date The date to adjust, not null
   * @param tenor The tenor, not null
   * @return The date adjusted by a tenor
   * @throws IllegalStateException If the tenor is not backed by a {@link Period}
   */
  public static LocalDate adjustDateByTenor(final LocalDate date, final Tenor tenor) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.isTrue(!tenor.isBusinessDayTenor(), "Business day tenors must have a calendar and initial offset to adjust a date");
    return date.plus(tenor.getPeriod());
  }

  /**
   * Adjusts a {@link ZonedDateTime} by a tenor. If the tenor is backed by a {@link BusinessDayTenor}, the calendar and
   * spot days are used when adjusting.
   * @param date The date to adjust, not null
   * @param tenor The tenor, not null
   * @param calendar The calendar, not null
   * @param spotDays The number of days for spot, greater than or equal to zero
   * @return The date adjusted by a tenor
   */
  public static ZonedDateTime adjustDateByTenor(final ZonedDateTime date, final Tenor tenor, final Calendar calendar, final int spotDays) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(spotDays >= 0, "number of spot days must be greater than zero; have {}", spotDays);
    if (tenor.isBusinessDayTenor()) {
      int offset;
      final BusinessDayTenor bdt = tenor.getBusinessDayTenor();
      switch (bdt) {
        case OVERNIGHT:
          offset = 0;
          break;
        case TOM_NEXT:
          offset = 1;
          break;
        case SPOT_NEXT:
          offset = spotDays;
          break;
        default:
          throw new IllegalArgumentException("Did not recognise tenor " + tenor);
      }
      ZonedDateTime result = date;
      int count = 0;
      while (count < offset) {
        result = result.plusDays(1);
        if (calendar.isWorkingDay(result.toLocalDate())) {
          count++;
        }
      }
      result = result.plusDays(1);
      while (!calendar.isWorkingDay(result.toLocalDate())) {
        result = result.plusDays(1);
      }
      return result;
    }
    return date.plus(tenor.getPeriod());
  }

  /**
   * Adjusts a {@link LocalDateTime} by a tenor. If the tenor is backed by a {@link BusinessDayTenor}, the calendar and
   * spot days are used when adjusting.
   * @param date The date to adjust, not null
   * @param tenor The tenor, not null
   * @param calendar The calendar, not null
   * @param spotDays The number of days for spot, greater than or equal to zero
   * @return The date adjusted by a tenor
   */
  public static LocalDateTime adjustDateByTenor(final LocalDateTime date, final Tenor tenor, final Calendar calendar, final int spotDays) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(spotDays >= 0, "number of spot days must be greater than zero; have {}", spotDays);
    if (tenor.isBusinessDayTenor()) {
      int offset;
      final BusinessDayTenor bdt = tenor.getBusinessDayTenor();
      switch (bdt) {
        case OVERNIGHT:
          offset = 0;
          break;
        case TOM_NEXT:
          offset = 1;
          break;
        case SPOT_NEXT:
          offset = spotDays;
          break;
        default:
          throw new IllegalArgumentException("Did not recognise tenor " + tenor);
      }
      LocalDateTime result = date;
      int count = 0;
      while (count < offset) {
        result = result.plusDays(1);
        if (calendar.isWorkingDay(result.toLocalDate())) {
          count++;
        }
      }
      result = result.plusDays(1);
      while (!calendar.isWorkingDay(result.toLocalDate())) {
        result = result.plusDays(1);
      }
      return result;
    }
    return date.plus(tenor.getPeriod());
  }

  /**
   * Adjusts a {@link LocalDate} by a tenor. If the tenor is backed by a {@link BusinessDayTenor}, the calendar and
   * spot days are used when adjusting.
   * @param date The date to adjust, not null
   * @param tenor The tenor, not null
   * @param calendar The calendar, not null
   * @param spotDays The number of days for spot, greater than or equal to zero
   * @return The date adjusted by a tenor
   */
  public static LocalDate adjustDateByTenor(final LocalDate date, final Tenor tenor, final Calendar calendar, final int spotDays) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(spotDays >= 0, "number of spot days must be greater than zero; have {}", spotDays);
    if (tenor.isBusinessDayTenor()) {
      int offset;
      final BusinessDayTenor bdt = tenor.getBusinessDayTenor();
      switch (bdt) {
        case OVERNIGHT:
          offset = 0;
          break;
        case TOM_NEXT:
          offset = 1;
          break;
        case SPOT_NEXT:
          offset = spotDays;
          break;
        default:
          throw new IllegalArgumentException("Did not recognise tenor " + tenor);
      }
      LocalDate result = date;
      int count = 0;
      while (count < offset) {
        result = result.plusDays(1);
        if (calendar.isWorkingDay(result)) {
          count++;
        }
      }
      result = result.plusDays(1);
      while (!calendar.isWorkingDay(result)) {
        result = result.plusDays(1);
      }
      return result;
    }
    return date.plus(tenor.getPeriod());
  }
}
