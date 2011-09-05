/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * Simple implementation of {@code HolidaySource} where the only holidays are weekends.
 * <p>
 * This is designed for testing.
 */
public class WeekendHolidaySource implements HolidaySource {

  @Override
  public Holiday getHoliday(final UniqueId uniqueId) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Holiday getHoliday(final ObjectId objectId, final VersionCorrection versionCorrection) {
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

}
