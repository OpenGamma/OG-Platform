/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A {@code HolidaySource} implemented using an underlying {@code HolidayMaster}.
 * <p>
 * The {@link HolidaySource} interface provides holidays to the application via a narrow API.
 * This class provides the source on top of a standard {@link HolidayMaster}.
 */
@PublicSPI
public class MasterHolidaySource
    extends AbstractMasterSource<Holiday, HolidayDocument, HolidayMaster>
    implements HolidaySource {

  // an empty set of dates
  private static final ImmutableSet<LocalDate> EMPTY = ImmutableSet.of();

  private final boolean _cacheHolidayCalendars;
  private final ConcurrentMap<HolidaySearchRequest, ImmutableSet<LocalDate>> _cachedHolidays = new ConcurrentHashMap<>();

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
  public Collection<Holiday> get(HolidayType holidayType,
                                 ExternalIdBundle regionOrExchangeIds) {
    HolidaySearchRequest request = createNonCurrencySearchRequest(holidayType, regionOrExchangeIds);
    return processDocuments(getMaster().search(request));
  }

  @Override
  public Collection<Holiday> get(Currency currency) {
    HolidaySearchRequest request = createCurrencySearchRequest(currency);
    return processDocuments(getMaster().search(request));
  }

  private Collection<Holiday> processDocuments(HolidaySearchResult search) {
    return ImmutableList.<Holiday>copyOf(search.getHolidays());
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    if (_cacheHolidayCalendars) {
      ArgumentChecker.notNull(dateToCheck, "dateToCheck");
      ArgumentChecker.notNull(currency, "currency");
      if (isWeekend(dateToCheck)) {
        return true;
      }
    }
    HolidaySearchRequest request = createCurrencySearchRequest(currency);
    return isHoliday(request, dateToCheck);
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    if (_cacheHolidayCalendars) {
      ArgumentChecker.notNull(dateToCheck, "dateToCheck");
      ArgumentChecker.notNull(holidayType, "holidayType");
      ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");
      if (isWeekend(dateToCheck)) {
        return true;
      }
    }
    HolidaySearchRequest request = createNonCurrencySearchRequest(holidayType, regionOrExchangeIds);
    return isHoliday(request, dateToCheck);
  }

  private VersionCorrection getVersionCorrection() {
    ServiceContext serviceContext = ThreadLocalServiceContext.getInstance();
    return serviceContext.get(VersionCorrectionProvider.class).getConfigVersionCorrection();
  }

  private HolidaySearchRequest createCurrencySearchRequest(Currency currency) {
    return createdVersionCorrectedSearchRequest(new HolidaySearchRequest(currency));
  }

  private HolidaySearchRequest createNonCurrencySearchRequest(HolidayType holidayType,
                                                              ExternalIdBundle regionOrExchangeIds) {
    return createdVersionCorrectedSearchRequest(new HolidaySearchRequest(holidayType, regionOrExchangeIds));
  }

  private HolidaySearchRequest createdVersionCorrectedSearchRequest(HolidaySearchRequest searchRequest) {
    searchRequest.setVersionCorrection(getVersionCorrection());
    return searchRequest;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    if (_cacheHolidayCalendars) {
      ArgumentChecker.notNull(dateToCheck, "dateToCheck");
      ArgumentChecker.notNull(holidayType, "holidayType");
      ArgumentChecker.notNull(regionOrExchangeId, "regionOrExchangeId");
      if (isWeekend(dateToCheck)) {
        return true;
      }
    }
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

    if (_cacheHolidayCalendars) {
      ImmutableSet<LocalDate> cachedDates = _cachedHolidays.get(request);
      if (cachedDates != null) {
        return cachedDates.contains(dateToCheck);
      }
      // get all holidays and cache
      HolidayDocument doc = getMaster().search(request).getFirstDocument();
      HolidaySearchRequest cacheKey = request.clone();
      if (doc == null) {
        _cachedHolidays.put(cacheKey, EMPTY);
      } else {
        _cachedHolidays.put(cacheKey, ImmutableSet.copyOf(doc.getHoliday().getHolidayDates()));
      }
      return isHoliday(doc, dateToCheck);
    }
    // Not caching, search for this date only.
    request.setDateToCheck(dateToCheck);
    HolidayDocument doc = getMaster().search(request).getFirstDocument();
    return isHoliday(doc, dateToCheck);
  }

  /**
   * Checks if the specified date is a holiday.
   * 
   * @param doc document retrieved from underlying holiday master, may be null
   * @param dateToCheck the date to check, not null
   * @return false if nothing was retrieved from underlying holiday master.
   *  Otherwise, true if and only if the date is a holiday based on the underlying holiday master
   */
  protected boolean isHoliday(final HolidayDocument doc, final LocalDate dateToCheck) {
    if (doc == null) {
      return false;
    }
    return Collections.binarySearch(doc.getHoliday().getHolidayDates(), dateToCheck) >= 0;
  }

  /**
   * Checks if the date is at the weekend, defined as a Saturday or Sunday.
   * 
   * @param date the date to check, not null
   * @return true if it is a weekend
   */
  protected boolean isWeekend(LocalDate date) {
    // avoids calling date.getDayOfWeek() twice
    return date.getDayOfWeek().getValue() >= 6;
  }

}
