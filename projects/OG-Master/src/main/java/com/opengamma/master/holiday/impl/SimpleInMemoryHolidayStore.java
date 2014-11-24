/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
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
 * Simple implementation of a container of holidays per calendar relying on a map. The putAll method can be used to
 * add calendars and their corresponding holidays to the store. The other methods are part of the {@link HolidaySource}
 * interface and allow to check whether a date is a holiday for a calendar.
 *
 * The methods to get a collection of holidays for a currency or to check with a date is a holiday for a currency
 * (rather than a calendar) are not implemented.
 *
 * {@link HolidayType} is ignored, only one holiday series is stored (or queried per id) - the holiday type information
 * on both the insertion (and query) is ignored).
 */
public class SimpleInMemoryHolidayStore implements HolidaySource {

  private Map<ExternalId, Holiday> _calendarHolidays = Maps.newHashMap();

  /**
   * @param calendarHolidays a map from a calendar or region id to a holiday that will be added to the store
   */
  public void putAll(Map<ExternalId, Holiday> calendarHolidays) {
    for (Entry<ExternalId, Holiday> entry : calendarHolidays.entrySet()) {
      _calendarHolidays.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Get the holiday series, null if not found. Ignores version of uniqueId
   * @param uniqueId  the unique identifier to search for, not null
   * @throws DataNotFoundException if the object could not be found
   * @return the series
   */
  @Override
  public Holiday get(UniqueId uniqueId) {
    Holiday holiday = _calendarHolidays.get(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue()));
    if (holiday == null) {
      throw new DataNotFoundException("Holiday with id " + uniqueId + " not found");
    }
    return holiday;
  }

  /**
   * Get the holiday series, null if not found. Ignores version of uniqueId
   * @param objectId the objectid of the series
   * @param versionCorrection version correction, which is ignored
   * @throws DataNotFoundException if the object could not be found
   * @return the series
   */
  @Override
  public Holiday get(ObjectId objectId, VersionCorrection versionCorrection) {
    Holiday holiday = _calendarHolidays.get(ExternalId.of(objectId.getScheme(), objectId.getValue()));
    if (holiday == null) {
      throw new DataNotFoundException("Holiday with id " + objectId + " not found");
    }
    return holiday;
  }

  @Override
  public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, Holiday> holidays = Maps.newHashMap();
    for (UniqueId id : uniqueIds) {
      holidays.put(id, get(id));
    }
    return holidays;
  }

  @Override
  public Map<ObjectId, Holiday> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    Map<ObjectId, Holiday> holidays = Maps.newHashMap();
    
    for (ObjectId obj : objectIds) {
      holidays.put(obj, _calendarHolidays.get(ExternalId.of(obj.getScheme(), obj.getValue())));
    }
    return holidays;
  }

  @Override
  public Set<Holiday> get(HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    Set<Holiday> holidays = new HashSet<>();
    for (ExternalId id : regionOrExchangeIds.getExternalIds()) {
      if (_calendarHolidays.containsKey(id)) {
        holidays.add(_calendarHolidays.get(id));
      }
    }
    return holidays;
  }

  @Override
  public Collection<Holiday> get(Currency currency) {
    throw new UnsupportedOperationException("Cannot check if date is a holiday without a calendar or region id");
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    throw new UnsupportedOperationException("Cannot check if date is a holiday without a calendar or region id");
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    
    for (ExternalId id : regionOrExchangeIds.getExternalIds()) {
      if (isHoliday(dateToCheck, holidayType, id)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId) {
    return isWeekend(dateToCheck) || isHoliday(dateToCheck, regionOrExchangeId);
  }
  
  private boolean isHoliday(LocalDate dateToCheck, ExternalId id) {
    if (_calendarHolidays.containsKey(id)) {
      return _calendarHolidays.get(id).getHolidayDates().contains(dateToCheck);
    } else {
      return false;
    }
  }
  
  /**
   * Checks if the date is at the weekend, defined as a Saturday or Sunday.
   * 
   * @param date the date to check, not null
   * @return true if it is a weekend
   */
  private boolean isWeekend(LocalDate date) {
    // avoids calling date.getDayOfWeek() twice
    return date.getDayOfWeek().getValue() >= 6;
  }

}
