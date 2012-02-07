/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.net.URI;

import javax.time.calendar.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeBooleanWrapper;
import com.opengamma.util.money.Currency;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link HolidaySource}.
 */
public class RemoteHolidaySource extends AbstractRemoteClient implements HolidaySource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteHolidaySource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public Holiday getHoliday(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataHolidaySourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Holiday.class);
  }

  @Override
  public Holiday getHoliday(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataHolidaySourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Holiday.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(currency, "currency");
    
    URI uri = DataHolidaySourceResource.uriSearchCheck(getBaseUri(), dateToCheck, HolidayType.CURRENCY, currency, null);
    return accessRemote(uri).get(FudgeBooleanWrapper.class).isValue();
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId) {
    return isHoliday(dateToCheck, holidayType, ExternalIdBundle.of(regionOrExchangeId));
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");
    
    URI uri = DataHolidaySourceResource.uriSearchCheck(getBaseUri(), dateToCheck, holidayType, null, regionOrExchangeIds);
    return accessRemote(uri).get(FudgeBooleanWrapper.class).isValue();
  }

}
