/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for managing the business day convention.
 * <p>
 * This is a thread-safe static utility class.
 */
class CacheItem {

  private final Map<LocalDate, Integer> distanceCache = new ConcurrentHashMap<>();
  private LocalDate minDate = null;
  private LocalDate maxDate = null;

  public CacheItem(LocalDate minDate, LocalDate maxDate, Calendar calendar) {
    distanceCache.put(minDate, 0);
    this.minDate = minDate;
    this.maxDate = minDate;
    update(maxDate, calendar);
  }

  public void update(LocalDate date, Calendar calendar) {

    int minIndex = distanceCache.get(minDate);
    LocalDate min = minDate;
    while (!min.isBefore(date)) {
      min = min.minusDays(1);
      if (calendar.isWorkingDay(min)) {
        distanceCache.put(min, --minIndex);
        minDate = min;
      }
    }

    int maxIndex = distanceCache.get(maxDate);
    LocalDate max = maxDate;
    while (!max.isAfter(date)) {
      max = max.plusDays(1);
      if (calendar.isWorkingDay(max)) {
        distanceCache.put(max, ++maxIndex);
        maxDate = max;
      }
    }
  }

  public int getIndex(LocalDate date, Calendar calendar) {
    while (!calendar.isWorkingDay(date)) {
      date = date.plusDays(1);
    }
    update(date, calendar);
    return distanceCache.get(date);
  }
}

public class BusinessDayDateUtils {

  private static final Map<String, CacheItem> CACHE = new ConcurrentHashMap<>();

  /**
   * Restricted constructor.
   */
  protected BusinessDayDateUtils() {
    super();
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates the number of days in between two dates with the date count
   * rule specified by the {@code TemporalAdjuster}.
   * 
   * @param startDate  the start date-time, not null
   * @param includeStart  whether to include the start
   * @param endDate  the end date-time, not null
   * @param includeEnd  whether to include the end
   * @param convention  the date adjuster, not null
   * @return the number of days between two dates
   */
  public static int getDaysBetween(final ZonedDateTime startDate, final boolean includeStart, final ZonedDateTime endDate, final boolean includeEnd, final TemporalAdjuster convention) {
    LocalDate date = startDate.toLocalDate();
    LocalDate localEndDate = endDate.toLocalDate();
    int mult = 1;
    if (startDate.isAfter(endDate)) {
      date = endDate.toLocalDate();
      localEndDate = startDate.toLocalDate();
      mult = -1;
    }
    int result = includeStart ? 1 : 0;
    while (!date.with(convention).equals(localEndDate)) {
      date = date.with(convention);
      result++;
    }
    return mult * (includeEnd ? result : result - 1);
  }

  /**
   * Add a certain number of working days (defined by the holidayCalendar) to a date 
   * @param startDate The start date
   * @param workingDaysToAdd working days to add
   * @param holidayCalendar Defines what is a non-working day
   * @return a working day
   */
  public static LocalDate addWorkDays(final LocalDate startDate, final int workingDaysToAdd, final Calendar holidayCalendar) {
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.notNull(holidayCalendar, "null holidayCalendar");

    int daysLeft = workingDaysToAdd;
    LocalDate temp = startDate;
    while (daysLeft > 0) {
      temp = temp.plusDays(1);
      if (holidayCalendar.isWorkingDay(temp)) {
        daysLeft--;
      }
    }
    return temp;
  }

  /**
   * Get the number of business days between two dates
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates
   */
  public static int getDaysBetween(LocalDate firstDate, LocalDate secondDate, Calendar calendar) {
    ArgumentChecker.notNull(firstDate, "first date");
    ArgumentChecker.notNull(secondDate, "second date");
    if (!(secondDate.isAfter(firstDate) || secondDate.equals(firstDate))) {
      throw new OpenGammaRuntimeException("d2 must be on or after d1: have d1 = " + firstDate + " and d2 = " + secondDate);
    }
    ArgumentChecker.notNull(calendar, "calendar");

    CacheItem cacheItem = CACHE.get(calendar.getName());
    if (cacheItem == null) {
      cacheItem = new CacheItem(firstDate, secondDate, calendar);
      CACHE.put(calendar.getName(), cacheItem);
    }
    return cacheItem.getIndex(secondDate, calendar) - cacheItem.getIndex(firstDate, calendar);
		/* the original OG code
    LocalDate date = firstDate;
    while (!calendar.isWorkingDay(date)) {
      date = date.plusDays(1);
    }
    date = date.plusDays(1);
    int count = 0;
    while (!date.isAfter(secondDate)) {
      if (calendar.isWorkingDay(date)) {
        count++;
      }
      date = date.plusDays(1);
    }
		return count;*/
  }

  /**
   * Get the number of business days between two dates. <b>Note:<b> these {@link ZonedDateTime} dates are converted to {@link LocalDate}, so any time-of-day and time zone information is lost
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates
   */
  public static int getDaysBetween(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final Calendar calendar) {
    ArgumentChecker.notNull(firstDate, "first date");
    ArgumentChecker.notNull(secondDate, "second date");
    return getDaysBetween(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }
}
