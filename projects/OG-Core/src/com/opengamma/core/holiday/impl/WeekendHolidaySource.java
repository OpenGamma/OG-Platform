/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.*;
import com.opengamma.util.money.Currency;

/**
 * Simple implementation of {@code HolidaySource} where the only holidays are weekends.
 * <p>
 * This is designed for testing.
 */
public class WeekendHolidaySource implements HolidaySource {

  @Override
  public Holiday get(final UniqueId uniqueId) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    return isWeekend(dateToCheck);
  }
  
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
      final ExternalIdBundle regionOrExchangeIds) {
    return isWeekend(dateToCheck);
  }
  
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
      final ExternalId regionOrExchangeId) {
    return isWeekend(dateToCheck);
  }
  
  private boolean isWeekend(final LocalDate dateToCheck) {
    return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
  }

  @Override
  public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, Holiday> result = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        Holiday object = get(uniqueId);
        result.put(uniqueId, object);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }
}
