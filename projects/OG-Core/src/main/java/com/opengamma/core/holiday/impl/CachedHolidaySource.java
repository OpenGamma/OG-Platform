/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.LocalDate;

import com.opengamma.core.AbstractSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.map.HashMap2;
import com.opengamma.util.map.HashMap3;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.Map3;
import com.opengamma.util.money.Currency;

/**
 * A cached {@link HolidaySource} using a concurrent hash map and
 * no eviction policy. This is better than having no cache but is
 * not very efficient. Also does not listen for changes to the underlying
 * data.
 */
public class CachedHolidaySource extends AbstractSource<Holiday> implements HolidaySource {

  private static final Object NULL = new Object();

  private final HolidaySource _underlying;
  private final ConcurrentMap<UniqueId, Object> _getHoliday1 = new ConcurrentHashMap<>();
  private final Map2<VersionCorrection, ObjectId, Object> _getHoliday2 = new HashMap2<>(HashMap2.WEAK_KEYS);
  private final Map<Currency, Object> _getHoliday3 = new ConcurrentHashMap<>();
  private final Map2<HolidayType, ExternalIdBundle, Object> _getHoliday4 = new HashMap2<>(HashMap2.WEAK_KEYS);
  private final ConcurrentMap<Currency, ConcurrentMap<LocalDate, Object>> _isHoliday1 = new ConcurrentHashMap<>();
  private final Map3<LocalDate, HolidayType, ExternalIdBundle, Object> _isHoliday2 = new HashMap3<>();
  private final Map3<LocalDate, HolidayType, ExternalId, Object> _isHoliday3 = new HashMap3<>();

  public CachedHolidaySource(final HolidaySource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected HolidaySource getUnderlying() {
    return _underlying;
  }

  @SuppressWarnings("unchecked")
  protected <T> T getOrThrow(final Object o) {
    if (o instanceof RuntimeException) {
      throw (RuntimeException) o;
    } else if (o == NULL) {
      return null;
    } else {
      return (T) o;
    }
  }

  protected Object safeNull(final Object o) {
    if (o != null) {
      return o;
    } else {
      return NULL;
    }
  }

  @Override
  public Holiday get(final UniqueId uniqueId) {
    Object result = _getHoliday1.get(uniqueId);
    if (result != null) {
      return getOrThrow(result);
    }
    try {
      final Holiday h = getUnderlying().get(uniqueId);
      result = _getHoliday1.putIfAbsent(uniqueId, safeNull(h));
      if (result != null) {
        return getOrThrow(result);
      }
      return h;
    } catch (RuntimeException ex) {
      _getHoliday1.putIfAbsent(uniqueId, ex);
      throw ex;
    }
  }

  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    Object result = _getHoliday2.get(versionCorrection, objectId);
    if (result != null) {
      return getOrThrow(result);
    }
    try {
      final Holiday h = getUnderlying().get(objectId, versionCorrection);
      result = _getHoliday2.putIfAbsent(versionCorrection, objectId, safeNull(h));
      if (result != null) {
        return getOrThrow(result);
      }
      return h;
    } catch (RuntimeException ex) {
      _getHoliday2.putIfAbsent(versionCorrection, objectId, ex);
      throw ex;
    }
  }

  @Override
  public Collection<Holiday> get(Currency currency) {
    Object result = _getHoliday3.get(currency);
    if (result != null) {
      return getOrThrow(result);
    }
    try {
      Collection<Holiday> holidays = getUnderlying().get(currency);
      result = _getHoliday3.putIfAbsent(currency, holidays);
      if (result != null) {
        return getOrThrow(result);
      } else {
        return holidays;
      }
    } catch (RuntimeException ex) {
      _getHoliday3.putIfAbsent(currency, ex);
      throw ex;
    }
  }

  @Override
  public Collection<Holiday> get(HolidayType holidayType,
                                 ExternalIdBundle regionOrExchangeIds) {
    Object result = _getHoliday4.get(holidayType, regionOrExchangeIds);
    if (result != null) {
      return getOrThrow(result);
    }
    try {
      Collection<Holiday> holidays = getUnderlying().get(holidayType, regionOrExchangeIds);
      result = _getHoliday4.putIfAbsent(holidayType, regionOrExchangeIds, safeNull(holidays));
      if (result != null) {
        return getOrThrow(result);
      } else {
        return holidays;
      }
    } catch (RuntimeException ex) {
      _getHoliday4.putIfAbsent(holidayType, regionOrExchangeIds, ex);
      throw ex;
    }
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    ConcurrentMap<LocalDate, Object> dates = _isHoliday1.get(currency);
    if (dates == null) {
      dates = new ConcurrentHashMap<>();
      final ConcurrentMap<LocalDate, Object> existing = _isHoliday1.putIfAbsent(currency, dates);
      if (existing != null) {
        dates = existing;
      }
    }
    Object result = dates.get(dateToCheck);
    if (result != null) {
      return (Boolean) getOrThrow(result);
    }
    try {
      final boolean isHoliday = getUnderlying().isHoliday(dateToCheck, currency);
      dates.putIfAbsent(dateToCheck, isHoliday);
      return isHoliday;
    } catch (RuntimeException ex) {
      dates.putIfAbsent(dateToCheck, ex);
      throw ex;
    }
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    Object result = _isHoliday2.get(dateToCheck, holidayType, regionOrExchangeIds);
    if (result != null) {
      return (Boolean) getOrThrow(result);
    }
    try {
      final boolean isHoliday = getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeIds);
      _isHoliday2.putIfAbsent(dateToCheck, holidayType, regionOrExchangeIds, isHoliday);
      return isHoliday;
    } catch (RuntimeException ex) {
      _isHoliday2.putIfAbsent(dateToCheck, holidayType, regionOrExchangeIds, ex);
      throw ex;
    }
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    Object result = _isHoliday3.get(dateToCheck, holidayType, regionOrExchangeId);
    if (result != null) {
      return (Boolean) getOrThrow(result);
    }
    try {
      final boolean isHoliday = getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeId);
      _isHoliday3.putIfAbsent(dateToCheck, holidayType, regionOrExchangeId, isHoliday);
      return isHoliday;
    } catch (RuntimeException ex) {
      _isHoliday3.putIfAbsent(dateToCheck, holidayType, regionOrExchangeId, ex);
      throw ex;
    }
  }

}
