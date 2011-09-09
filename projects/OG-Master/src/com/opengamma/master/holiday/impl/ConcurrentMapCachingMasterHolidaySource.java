/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * PLAT-1015: A cache to optimize the results of {@code MasterHolidaySource}.
 */

public class ConcurrentMapCachingMasterHolidaySource extends MasterHolidaySource {
  //TODO timeout

  /**
   * Creates the cache around an underlying holiday source.
   * 
   * @param underlying  the underlying data, not null
   */
  public ConcurrentMapCachingMasterHolidaySource(final HolidayMaster underlying) {
    super(underlying);
  }

  private class Entry {
    long expiry; // CSIGNORE //TODO this
    // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data
    HolidayDocument value; // CSIGNORE
  }
  
  private ConcurrentHashMap<HolidaySearchRequest, Entry> _cache = new ConcurrentHashMap<HolidaySearchRequest, Entry>();
  
  
  private ConcurrentHashMap<Pair<HolidayType, ExternalIdBundle>, Entry> _cache2 = new ConcurrentHashMap<Pair<HolidayType, ExternalIdBundle>, Entry>();

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    
    //PLAT-1015 : avoid cloning regionOrExchangeIds like the plague, we get nice cheap equals on hits then
    ObjectsPair<HolidayType, ExternalIdBundle> cacheKey = Pair.of(holidayType, regionOrExchangeIds);
    Entry loaded = _cache2.get(cacheKey);
    if (loaded == null) {
      HolidaySearchRequest searchRequest = getSearchRequest(holidayType, regionOrExchangeIds);
      HolidayDocument newDoc = getDocCached(searchRequest);
      Entry entry = new Entry();
      entry.value = newDoc;
      loaded = entry;
      _cache2.put(cacheKey, entry);
    }
    return isHoliday(loaded.value, dateToCheck);
  }
  
  //-------------------------------------------------------------------------
  @Override
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    HolidayDocument doc = getDocCached(request);
    return isHoliday(doc, dateToCheck);
  }

  private HolidayDocument getDocCached(final HolidaySearchRequest request) {
    HolidayDocument doc; 
    Entry entry = _cache.get(request);
    if (entry != null) {
      doc = entry.value;
    } else {
      doc = getMaster().search(request).getFirstDocument();
      
      entry = new Entry();
      entry.value = doc;
      _cache.putIfAbsent(request, entry);
    }
    return doc;
  }
}
