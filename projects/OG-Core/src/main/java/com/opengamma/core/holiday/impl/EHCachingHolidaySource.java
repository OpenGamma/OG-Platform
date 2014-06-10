/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.putException;
import static com.opengamma.util.ehcache.EHCacheUtils.putValue;

import java.util.Arrays;
import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.opengamma.core.AbstractEHCachingSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * An EHCache based {@link HolidaySource}. This is better than having no cache but is not very efficient. Also does not listen for changes to the underlying data.
 */
public class EHCachingHolidaySource extends AbstractEHCachingSource<Holiday, HolidaySource> implements HolidaySource {

  /*package*/static final String CACHE_NAME = "holiday";
  private final Cache _cache;

  public EHCachingHolidaySource(final HolidaySource underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  protected Cache getCache() {
    return _cache;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    final Object key = Arrays.asList(dateToCheck, currency);
    final Element e = getCache().get(key);
    if (e != null) {
      return (Boolean) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().isHoliday(dateToCheck, currency), getCache());
    } catch (RuntimeException ex) {
      return (Boolean) putException(key, ex, getCache());
    }
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    final Object key = Arrays.asList(dateToCheck, holidayType, regionOrExchangeIds);
    final Element e = getCache().get(key);
    if (e != null) {
      return (Boolean) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeIds), getCache());
    } catch (RuntimeException ex) {
      return (Boolean) putException(key, ex, getCache());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Holiday> get(HolidayType holidayType,
                                 ExternalIdBundle regionOrExchangeIds) {
    Object key = Arrays.asList(holidayType, regionOrExchangeIds);
    Element e = getCache().get(key);
    if (e != null) {
      return (Collection<Holiday>) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().get(holidayType, regionOrExchangeIds), getCache());
    } catch (RuntimeException ex) {
      return (Collection<Holiday>) putException(key, ex, getCache());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Holiday> get(Currency currency) {
    Element e = getCache().get(currency);
    if (e != null) {
      return (Collection<Holiday>) EHCacheUtils.get(e);
    }
    try {
      return putValue(currency, getUnderlying().get(currency), getCache());
    } catch (RuntimeException ex) {
      return (Collection<Holiday>) putException(currency, ex, getCache());
    }
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    final Object key = Arrays.asList(dateToCheck, holidayType, regionOrExchangeId);
    final Element e = getCache().get(key);
    if (e != null) {
      return (Boolean) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeId), getCache());
    } catch (RuntimeException ex) {
      return (Boolean) putException(key, ex, getCache());
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  @Override
  public void shutdown() {
    super.shutdown();
    _cache.getCacheManager().removeCache(CACHE_NAME);
  }

}
