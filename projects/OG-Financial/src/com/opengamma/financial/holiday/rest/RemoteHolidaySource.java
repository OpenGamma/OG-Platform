/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.holiday.rest;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Provides remote access to a {@link HolidaySource}.
 */
public class RemoteHolidaySource implements HolidaySource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteHolidaySource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public Holiday getHoliday(final UniqueId uniqueId) {
    final RestTarget target = getTargetBase().resolveBase("holidayUID").resolve(uniqueId.toString());
    final Holiday holiday;
    try {
      holiday = getRestClient().getSingleValue(Holiday.class, target, "holiday");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    if (holiday == null) {
      throw new DataNotFoundException(target.toString());
    }
    return holiday;
  }

  @Override
  public Holiday getHoliday(final ObjectId objectId, final VersionCorrection versionCorrection) {
    final RestTarget target = getTargetBase().resolveBase("holidayOID").resolveBase(objectId.toString()).resolveBase(versionCorrection.getVersionAsOfString()).resolve(
        versionCorrection.getCorrectedToString());
    final Holiday holiday;
    try {
      holiday = getRestClient().getSingleValue(Holiday.class, target, "holiday");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    if (holiday == null) {
      throw new DataNotFoundException(target.toString());
    }
    return holiday;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    return getRestClient().getSingleValue(Boolean.class, getTargetBase().resolveBase("holidayCurrency").resolveBase(dateToCheck.toString()).resolve(currency.getCode()), "holiday");
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    return getRestClient().getSingleValue(Boolean.class,
        getTargetBase().resolveBase("holidayBundle").resolveBase(dateToCheck.toString()).resolveBase(holidayType.toString()).resolveQuery("id", regionOrExchangeIds.toStringList()), "holiday");
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    return getRestClient().getSingleValue(Boolean.class,
        getTargetBase().resolveBase("holidayIdentifier").resolveBase(dateToCheck.toString()).resolveBase(holidayType.toString()).resolve(regionOrExchangeId.toString()), "holiday");
  }

}
