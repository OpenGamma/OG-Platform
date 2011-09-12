/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.holiday.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class HolidaySourceResource {

  private final HolidaySource _underlying;
  private final FudgeContext _fudgeContext;

  public HolidaySourceResource(final HolidaySource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected HolidaySource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  private FudgeMsgEnvelope serializeHoliday(final Holiday holiday) {
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg response = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(response, "holiday", null, holiday, Holiday.class);
    return new FudgeMsgEnvelope(response);
  }

  private FudgeMsgEnvelope serializeBoolean(final boolean holiday) {
    final MutableFudgeMsg response = getFudgeContext().newMessage();
    response.add("holiday", holiday);
    return new FudgeMsgEnvelope(response);
  }

  @GET
  @Path("holidayUID/{uniqueId}")
  public FudgeMsgEnvelope getHoliday(@PathParam("uniqueId") String uniqueIdString) {
    final UniqueId uniqueId = UniqueId.parse(uniqueIdString);
    return serializeHoliday(getUnderlying().getHoliday(uniqueId));
  }

  @GET
  @Path("holidayOID/{objectId}/{version}/{correction}")
  public FudgeMsgEnvelope getHoliday(@PathParam("objectId") String objectIdString, @PathParam("version") String versionString, @PathParam("correction") String correctionString) {
    final ObjectId objectId = ObjectId.parse(objectIdString);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionString, correctionString);
    return serializeHoliday(getUnderlying().getHoliday(objectId, versionCorrection));
  }

  @GET
  @Path("holidayCurrency/{dateToCheck}/{currency}")
  public FudgeMsgEnvelope isHoliday(@PathParam("dateToCheck") String dateToCheckString, @PathParam("currency") String currencyString) {
    final LocalDate dateToCheck = LocalDate.parse(dateToCheckString);
    final Currency currency = Currency.of(currencyString);
    return serializeBoolean(getUnderlying().isHoliday(dateToCheck, currency));
  }

  @GET
  @Path("holidayBundle/{dateToCheck}/{holidayType}")
  public FudgeMsgEnvelope isHoliday(@PathParam("dateToCheck") String dateToCheckString, @PathParam("holidayType") String holidayTypeString, @QueryParam("id") List<String> bundleStrings) {
    final LocalDate dateToCheck = LocalDate.parse(dateToCheckString);
    final HolidayType holidayType = HolidayType.valueOf(holidayTypeString);
    final Collection<ExternalId> externalIds = new ArrayList<ExternalId>(bundleStrings.size());
    for (String bundleString : bundleStrings) {
      externalIds.add(ExternalId.parse(bundleString));
    }
    return serializeBoolean(getUnderlying().isHoliday(dateToCheck, holidayType, ExternalIdBundle.of(externalIds)));
  }

  @GET
  @Path("holidayIdentifier/{dateToCheck}/{holidayType}/{regionOrExchangeId}")
  public FudgeMsgEnvelope isHoliday(@PathParam("dateToCheck") String dateToCheckString, @PathParam("holidayType") String holidayTypeString,
      @PathParam("regionOrExchangeId") String regionOrExchangeIdString) {
    final LocalDate dateToCheck = LocalDate.parse(dateToCheckString);
    final HolidayType holidayType = HolidayType.valueOf(holidayTypeString);
    final ExternalId regionOrExchangeId = ExternalId.parse(regionOrExchangeIdString);
    return serializeBoolean(getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeId));
  }

}
