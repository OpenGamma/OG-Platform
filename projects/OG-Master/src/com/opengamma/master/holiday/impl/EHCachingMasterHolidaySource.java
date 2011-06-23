/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache to optimize the results of {@code MasterHolidaySource}.
 */
public class EHCachingMasterHolidaySource extends MasterHolidaySource {

  /**
   * Cache key for holidays.
   */
  private static final String HOLIDAY_CACHE = "holiday";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The result cache.
   */
  private final Cache _holidayCache;

  /**
   * Creates the cache around an underlying holiday source.
   * 
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingMasterHolidaySource(final HolidayMaster underlying, final CacheManager cacheManager) {
    super(underlying);
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, HOLIDAY_CACHE);
    _holidayCache = EHCacheUtils.getCacheFromManager(cacheManager, HOLIDAY_CACHE);
  }

  //-------------------------------------------------------------------------
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    Element e = _holidayCache.get(request);
    if (e != null) {
      HolidayDocument doc = (HolidayDocument) e.getValue();
      return isHoliday(doc, dateToCheck);
    } else {
      HolidayDocument doc = getMaster().search(request).getFirstDocument();
      
      Element element = new Element(request, doc);
      element.setTimeToLive(10); // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data
      _holidayCache.put(element);
      return isHoliday(doc, dateToCheck);
    }
  }

}
