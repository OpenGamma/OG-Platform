/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
 * 
 */
public class SimpleInMemoryHolidaySource implements HolidaySource {

  private Map<UniqueId, Holiday> _calendarHolidays = Maps.newHashMap();
  private Multimap<Currency, Holiday> _currencyHolidays = HashMultimap.create();
 
  public void putAll(Map<UniqueId, Holiday> calendarHolidays) {
    _calendarHolidays.putAll(calendarHolidays);
  }
  
  public void putAll(Multimap<Currency, Holiday> currencyHolidays) {
    _currencyHolidays.putAll(currencyHolidays);
  }

  @Override
  public Holiday get(UniqueId uniqueId) {
    return _calendarHolidays.get(uniqueId);
  }

  @Override
  public Holiday get(ObjectId objectId, VersionCorrection versionCorrection) {
    return _calendarHolidays.get(UniqueId.of(objectId, versionCorrection.getVersionAsOfString()));
  }

  @Override
  public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, Holiday> holidays = Maps.newHashMap();
    
    for (UniqueId id : uniqueIds) {
      holidays.put(id, _calendarHolidays.get(id));
    }
    return holidays;
  }

  @Override
  public Map<ObjectId, Holiday> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    Map<ObjectId, Holiday> holidays = Maps.newHashMap();
    
    for (ObjectId obj : objectIds) {
      holidays.put(obj, _calendarHolidays.get(UniqueId.of(obj, versionCorrection.getVersionAsOfString())));
    }
    return holidays;
  }

  @Override
  public Collection<Holiday> get(HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    ArrayList<Holiday> holidays = Lists.newArrayList();
    
    for (ExternalId id : regionOrExchangeIds.getExternalIds()) {
      if (_calendarHolidays.containsKey(id) && (_calendarHolidays.get(id).getType().compareTo(holidayType) == 0)) {
        holidays.add(_calendarHolidays.get(id));
      }
    }
    return holidays;
  }

  @Override
  public Collection<Holiday> get(Currency currency) {
    return _currencyHolidays.get(currency);
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    Collection<Holiday> holidays = get(currency);
    
    for (Holiday holiday : holidays) {
      if (holiday.getHolidayDates().contains(dateToCheck)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    Collection<Holiday> holidays = get(holidayType, regionOrExchangeIds);

    for (Holiday holiday : holidays) {
      if (holiday.getHolidayDates().contains(dateToCheck)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId) {
    Collection<Holiday> holidays = get(holidayType, ExternalIdBundle.of(regionOrExchangeId));

    for (Holiday holiday : holidays) {
      if (holiday.getHolidayDates().contains(dateToCheck)) {
        return true;
      }
    }
    return false;
  }

}
