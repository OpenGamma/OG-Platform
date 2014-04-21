/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Proxies on top of another {@link HolidaySource} and converts the schemes of all
 * identifiers requested from one scheme to another, but <em>ONLY</em> for the
 * {@code isHoliday} calls. For all other requests, the underlying is invoked
 * unmodified.
 * <strong>This class should only be used in conjunction with {@link HolidaySourceCalendarAdapter}.
 * <p/>
 * For the original requirements, see <a href="http://jira.opengamma.com/browse/PLAT-5498">PLAT-5498</a>.
 */
public class SchemeAlteringHolidaySource implements HolidaySource {
  private final HolidaySource _underlying;
  private final Map<String, String> _schemeMappings = new ConcurrentHashMap<String, String>();
  
  public SchemeAlteringHolidaySource(HolidaySource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }
  
  public void addMapping(String sourceScheme, String targetScheme) {
    ArgumentChecker.notNull(sourceScheme, "sourceScheme");
    ArgumentChecker.notNull(targetScheme, "targetScheme");
    _schemeMappings.put(sourceScheme, targetScheme);
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  public HolidaySource getUnderlying() {
    return _underlying;
  }
  
  protected String translateScheme(String scheme) {
    String result = _schemeMappings.get(scheme);
    if (result == null) {
      result = scheme;
    }
    return result;
  }
  
  protected ExternalId translateExternalId(ExternalId externalId) {
    String newScheme = translateScheme(externalId.getScheme().getName());
    ExternalId translatedId = ExternalId.of(newScheme, externalId.getValue());
    return translatedId;
  }
  
  @Override
  public Holiday get(UniqueId uniqueId) {
    return getUnderlying().get(uniqueId);
  }

  @Override
  public Holiday get(ObjectId objectId, VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Holiday> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return getUnderlying().get(objectIds, versionCorrection);
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    return getUnderlying().isHoliday(dateToCheck, currency);
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");
    
    Set<ExternalId> translatedIds = new HashSet<ExternalId>();
    for (ExternalId externalId : regionOrExchangeIds.getExternalIds()) {
      ExternalId translatedId = translateExternalId(externalId);
      translatedIds.add(translatedId);
    }
    ExternalIdBundle translatedBundle = ExternalIdBundle.of(translatedIds);
    
    return getUnderlying().isHoliday(dateToCheck, holidayType, translatedBundle);
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(regionOrExchangeId, "regionOrExchangeId");
    ExternalId translatedId = translateExternalId(regionOrExchangeId);
    return getUnderlying().isHoliday(dateToCheck, holidayType, translatedId);
  }

}
