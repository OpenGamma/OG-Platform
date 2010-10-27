/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.Currency;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * 
 */
public class EHCachingHolidaySource implements HolidaySource {
  
  /**
   * Cache key for holidays.
   */
  private static final String HOLIDAY_CACHE = "holiday";
  
  /**
   * The underlying holiday source.
   */
  private final HolidaySource _underlying;
  
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  
  /**
   * The result cache.
   */
  private final Cache _holiday;
  
  /**
   * Creates the cache around an underlying holiday source.
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingHolidaySource(final HolidaySource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, HOLIDAY_CACHE);
    _holiday = EHCacheUtils.getCacheFromManager(cacheManager, HOLIDAY_CACHE);
  }

  // -------------------------------------------------------------------------
  
  public HolidaySource getUnderlying() {
    return _underlying;
  }
  
  public CacheManager getCacheManager() {
    return _cacheManager;
  }
  
  // -------------------------------------------------------------------------  
  
  private static final class DateCurrencyKey {
    private final LocalDate _dateToCheck;
    private final Currency _currency;
    
    public DateCurrencyKey(
        LocalDate dateToCheck,
        Currency currency) {
      ArgumentChecker.notNull(dateToCheck, "dateToCheck");
      ArgumentChecker.notNull(currency, "currency");
      _dateToCheck = dateToCheck;
      _currency = currency;
    }
    
    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    DateCurrencyKey identifier = new DateCurrencyKey(dateToCheck, currency);
    Element e = _holiday.get(identifier);
    if (e != null) {
      return (Boolean) e.getValue();
    } else {
      boolean isHoliday = getUnderlying().isHoliday(dateToCheck, currency);
      _holiday.put(new Element(identifier, isHoliday));
      return isHoliday;
    }
  }
  
  private static final class DateTypeBundleKey {
    private final LocalDate _dateToCheck;
    private final HolidayType _holidayType;
    private final IdentifierBundle _regionOrExchangeIds;
    
    public DateTypeBundleKey(
        LocalDate dateToCheck,
        HolidayType holidayType,
        IdentifierBundle regionOrExchangeIds) {
      ArgumentChecker.notNull(dateToCheck, "dateToCheck");
      ArgumentChecker.notNull(holidayType, "holidayType");
      ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");
      _dateToCheck = dateToCheck;
      _holidayType = holidayType;
      _regionOrExchangeIds = regionOrExchangeIds;
    }
    
    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, IdentifierBundle regionOrExchangeIds) {
    DateTypeBundleKey identifier = new DateTypeBundleKey(dateToCheck, holidayType, regionOrExchangeIds);
    Element e = _holiday.get(identifier);
    if (e != null) {
      return (Boolean) e.getValue();
    } else {
      boolean isHoliday = getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeIds);
      _holiday.put(new Element(identifier, isHoliday));
      return isHoliday;
    }
  }
  
  private static final class DateTypeIdentifierKey {
    private final LocalDate _dateToCheck;
    private final HolidayType _holidayType;
    private final Identifier _regionOrExchangeId;
    
    public DateTypeIdentifierKey(
        LocalDate dateToCheck,
        HolidayType holidayType,
        Identifier regionOrExchangeId) {
      ArgumentChecker.notNull(dateToCheck, "dateToCheck");
      ArgumentChecker.notNull(holidayType, "holidayType");
      ArgumentChecker.notNull(regionOrExchangeId, "regionOrExchangeId");
      _dateToCheck = dateToCheck;
      _holidayType = holidayType;
      _regionOrExchangeId = regionOrExchangeId;
    }
    
    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, Identifier regionOrExchangeId) {
    DateTypeIdentifierKey identifier = new DateTypeIdentifierKey(dateToCheck, holidayType, regionOrExchangeId);
    Element e = _holiday.get(identifier);
    if (e != null) {
      return (Boolean) e.getValue();
    } else {
      boolean isHoliday = getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeId);
      _holiday.put(new Element(identifier, isHoliday));
      return isHoliday;
    }
  }

}
