/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.concurrent.ConcurrentHashMap;

import javax.time.Duration;
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

  /*
   * NOTE: this is only attempted
   */
  private final Duration _timeout = Duration.ofSeconds(10); // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data
  private final long _timeoutMillis = _timeout.toMillisLong();
  
  /**
   * Creates the cache around an underlying holiday source.
   * 
   * @param underlying  the underlying data, not null
   */
  public ConcurrentMapCachingMasterHolidaySource(final HolidayMaster underlying) {
    super(underlying);
  }

  private class Entry {
    final long expiry; // CSIGNORE //TODO this
    final HolidayDocument value; // CSIGNORE
    
    public Entry(HolidayDocument value) {
      this(System.currentTimeMillis() + _timeoutMillis, value);
    }
    
    public Entry(long expiry, HolidayDocument value) {
      super();
      this.expiry = expiry;
      this.value = value;
    }
  }
  
  //TODO: need to expire unused entries
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
    if (loaded == null || !isValid(loaded)) {
      HolidaySearchRequest searchRequest = getSearchRequest(holidayType, regionOrExchangeIds);
      HolidayDocument newDoc = getDocCached(searchRequest);
      Entry entry = new Entry(newDoc);
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
    Entry entry = _cache.get(request);
    if (entry == null || !isValid(entry)) {
      HolidayDocument doc = getMaster().search(request).getFirstDocument();

      entry = new Entry(doc);
      _cache.putIfAbsent(request, entry);
    }
    return entry.value;
  }
  
  private boolean isValid(Entry entry) {
    //NOTE: when this returns false we can have several thread hitting the database at once, which is a bit stupid
    long expiry = entry.expiry;
    long allowed = System.currentTimeMillis();
    return expiry <= allowed;
  }
}