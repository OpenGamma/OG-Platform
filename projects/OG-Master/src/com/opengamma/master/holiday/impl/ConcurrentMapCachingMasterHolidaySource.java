/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import javax.time.Duration;
import javax.time.calendar.LocalDate;

import org.springframework.context.Lifecycle;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * PLAT-1015: A cache to optimize the results of {@code MasterHolidaySource}.
 */

public class ConcurrentMapCachingMasterHolidaySource extends MasterHolidaySource implements Lifecycle {

  private final Duration _timeout = Duration.ofStandardMinutes(10); // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data

  private final PreemptiveCache<HolidaySearchRequest, HolidayDocument> _requestCache;
  private final PreemptiveCache<ObjectsPair<HolidayType, ExternalIdBundle>, HolidayDocument> _typeBundleCache;

  /**
   * Creates the cache around an underlying holiday source.
   * 
   * @param underlying  the underlying data, not null
   */
  public ConcurrentMapCachingMasterHolidaySource(final HolidayMaster underlying) {
    super(underlying);
    //TODO stop these PreemptiveCache
    _requestCache = new PreemptiveCache<HolidaySearchRequest, HolidayDocument>(_timeout) {

      @Override
      protected HolidayDocument getValueImpl(HolidaySearchRequest request) {
        return getMaster().search(request).getFirstDocument();
      }
    };
    _typeBundleCache = new PreemptiveCache<ObjectsPair<HolidayType, ExternalIdBundle>, HolidayDocument>(_timeout) {

      @Override
      protected HolidayDocument getValueImpl(ObjectsPair<HolidayType, ExternalIdBundle> request) {
        HolidaySearchRequest searchRequest = getSearchRequest(request.getFirst(), request.getSecond());
        return _requestCache.get(searchRequest);
      }
    };
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
      final ExternalIdBundle regionOrExchangeIds) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    //PLAT-1015 : avoid cloning regionOrExchangeIds like the plague, we get nice cheap equals on hits then
    HolidayDocument doc = _typeBundleCache.get(ObjectsPair.of(holidayType, regionOrExchangeIds));
    return isHoliday(doc, dateToCheck);
  }

  //-------------------------------------------------------------------------
  @Override
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    HolidayDocument doc = _requestCache.get(request);
    return isHoliday(doc, dateToCheck);
  }

  @Override
  public void start() {
    //Caches started by default
  }

  @Override
  public void stop() {
    _requestCache.stop();
    _typeBundleCache.stop();
  }

  @Override
  public boolean isRunning() {
    //TODO this
    return true;
  }
}
