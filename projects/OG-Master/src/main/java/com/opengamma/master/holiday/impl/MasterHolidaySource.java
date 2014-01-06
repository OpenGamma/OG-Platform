/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A {@code HolidaySource} implemented using an underlying {@code HolidayMaster}.
 * <p>
 * The {@link HolidaySource} interface provides holidays to the application via a narrow API. This class provides the source on top of a standard {@link HolidayMaster}.
 */
@PublicSPI
public class MasterHolidaySource extends AbstractMasterSource<Holiday, HolidayDocument, HolidayMaster> implements HolidaySource {
  private final boolean _cacheHolidayCalendars;
  private final ConcurrentMap<HolidaySearchRequest, List<LocalDate>> _cachedHolidays = new ConcurrentHashMap<HolidaySearchRequest, List<LocalDate>>();

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public MasterHolidaySource(final HolidayMaster master) {
    this(master, false);
  }

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   * @param cacheCalendars whether all calendars should be cached
   */
  public MasterHolidaySource(final HolidayMaster master, final boolean cacheCalendars) {
    super(master);
    _cacheHolidayCalendars = cacheCalendars;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    HolidaySearchRequest request = new HolidaySearchRequest(currency);
    return isHoliday(request, dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    HolidaySearchRequest request = getSearchRequest(holidayType, regionOrExchangeIds);
    return isHoliday(request, dateToCheck);
  }

  protected HolidaySearchRequest getSearchRequest(final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    HolidaySearchRequest request = new HolidaySearchRequest(holidayType, regionOrExchangeIds);
    return request;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    HolidaySearchRequest request = new HolidaySearchRequest(holidayType, ExternalIdBundle.of(regionOrExchangeId));
    return isHoliday(request, dateToCheck);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the specified date is a holiday.
   * 
   * @param request the request to search base on, not null
   * @param dateToCheck the date to check, not null
   * @return true if the date is a holiday
   */
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    if (isWeekend(dateToCheck)) {
      return true;
    }
    HolidaySearchRequest cacheKey = request.clone();

    if (_cacheHolidayCalendars) {
      List<LocalDate> cachedDates = _cachedHolidays.get(cacheKey);
      if (cachedDates != null) {
        if (cachedDates.isEmpty()) {
          // Sign that we couldn't find anything.
          return false;
        }
        return isHoliday(cachedDates, dateToCheck);
      }
    }

    HolidayDocument doc;
    if (_cacheHolidayCalendars) {
      // get all holidays and cache
      doc = getMaster().search(cacheKey).getFirstDocument();
      if (doc == null) {
        _cachedHolidays.put(cacheKey, Collections.<LocalDate>emptyList());
      } else {
        _cachedHolidays.put(cacheKey, doc.getHoliday().getHolidayDates());
      }
    } else {
      // Not caching, search for this date only.
      request.setDateToCheck(dateToCheck);
      doc = getMaster().search(request).getFirstDocument();
    }
    return isHoliday(doc, dateToCheck);
  }

  /**
   * Checks if the specified date is a holiday.
   * 
   * @param doc document retrieved from underlying holiday master, may be null
   * @param dateToCheck the date to check, not null
   * @return false if nothing was retrieved from underlying holiday master. Otherwise, true if and only if the date is a holiday based on the underlying holiday master
   */
  protected boolean isHoliday(final HolidayDocument doc, final LocalDate dateToCheck) {
    if (doc == null) {
      return false;
    }
    return Collections.binarySearch(doc.getHoliday().getHolidayDates(), dateToCheck) >= 0;
  }

  protected boolean isHoliday(final List<LocalDate> dates, final LocalDate dateToCheck) {
    return Collections.binarySearch(dates, dateToCheck) >= 0;
  }

  /**
   * Checks if the date is at the weekend, defined as a Saturday or Sunday.
   * 
   * @param date the date to check, not null
   * @return true if it is a weekend
   */
  protected boolean isWeekend(LocalDate date) {
    return (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
  }

}
