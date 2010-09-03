/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday;

import java.util.Collection;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;



/**
 * Interface for managing and detailed querying information on holidays
 */
public interface HolidayMaster {
  HolidayDocument getHoliday(UniqueIdentifier uniqueId);
  HolidaySearchResult searchHolidays(HolidaySearchRequest searchRequest);
  HolidaySearchResult searchHistoricHolidays(HolidaySearchHistoricRequest searchHistoricRequest);
  UniqueIdentifier addHoliday(Currency currency, Collection<LocalDate> holidayDates);
  UniqueIdentifier addHoliday(Identifier exchangeOrRegionId, HolidayType holidayType, Collection<LocalDate> holidayDates);
  HolidayDocument updateHoliday(HolidayDocument holidayDocument);
}
