/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.get;
import static com.opengamma.util.ehcache.EHCacheUtils.putException;
import static com.opengamma.util.ehcache.EHCacheUtils.putValue;

import java.util.Arrays;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;

/**
 * An EHCache based {@link HolidaySource}. This is better than having no cache but is not very efficient. Also does not listen for changes to the underlying data.
 */
public class EHCachingHolidaySource implements HolidaySource {

  /*package*/ static final String CACHE_NAME = "holiday";
  private final HolidaySource _underlying;
  private final Cache _cache;

  public EHCachingHolidaySource(final HolidaySource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  protected HolidaySource getUnderlying() {
    return _underlying;
  }

  protected Cache getCache() {
    return _cache;
  }

  @Override
  public Holiday getHoliday(final UniqueId uniqueId) {
    final Element e = getCache().get(uniqueId);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(uniqueId, getUnderlying().getHoliday(uniqueId), getCache());
    } catch (RuntimeException ex) {
      return putException(uniqueId, ex, getCache());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Holiday getHoliday(final ObjectId objectId, final VersionCorrection versionCorrection) {
    final Object key = Arrays.asList(objectId, versionCorrection);
    final Element e = getCache().get(key);
    if (e != null) {
      return get(e);
    }
    try {
      return putValue(key, getUnderlying().getHoliday(objectId, versionCorrection), getCache());
    } catch (RuntimeException ex) {
      return putException(key, ex, getCache());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    final Object key = Arrays.asList(dateToCheck, currency);
    final Element e = getCache().get(key);
    if (e != null) {
      return (Boolean) get(e);
    }
    try {
      return (Boolean) putValue(key, getUnderlying().isHoliday(dateToCheck, currency), getCache());
    } catch (RuntimeException ex) {
      return (Boolean) putException(key, ex, getCache());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    final Object key = Arrays.asList(dateToCheck, holidayType, regionOrExchangeIds);
    final Element e = getCache().get(key);
    if (e != null) {
      return (Boolean) get(e);
    }
    try {
      return (Boolean) putValue(key, getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeIds), getCache());
    } catch (RuntimeException ex) {
      return (Boolean) putException(key, ex, getCache());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    final Object key = Arrays.asList(dateToCheck, holidayType, regionOrExchangeId);
    final Element e = getCache().get(key);
    if (e != null) {
      return (Boolean) get(e);
    }
    try {
      return (Boolean) putValue(key, getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeId), getCache());
    } catch (RuntimeException ex) {
      return (Boolean) putException(key, ex, getCache());
    }
  }

}
