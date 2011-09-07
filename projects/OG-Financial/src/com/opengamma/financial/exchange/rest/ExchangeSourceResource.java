/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.exchange.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * 
 */
public class ExchangeSourceResource {

  private final ExchangeSource _underlying;
  private final FudgeContext _fudgeContext;

  public ExchangeSourceResource(final ExchangeSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected ExchangeSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  private FudgeMsgEnvelope serializeExchange(final Exchange exchange) {
    if (exchange == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg response = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(response, "exchange", null, exchange);
    return new FudgeMsgEnvelope(response);
  }

  @GET
  @Path("exchangeUID/{uniqueId}")
  public FudgeMsgEnvelope getExchange(@PathParam("uniqueId") String uniqueIdString) {
    final UniqueId uniqueId = UniqueId.parse(uniqueIdString);
    return serializeExchange(getUnderlying().getExchange(uniqueId));
  }

  @GET
  @Path("exchangeOID/{objectId}/{version}/{correction}")
  public FudgeMsgEnvelope getExchange(@PathParam("objectId") String objectIdString, @PathParam("version") String versionString, @PathParam("correction") String correctionString) {
    final ObjectId objectId = ObjectId.parse(objectIdString);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionString, correctionString);
    return serializeExchange(getUnderlying().getExchange(objectId, versionCorrection));
  }

  @GET
  @Path("exchanges/{version}/{correction}")
  public FudgeMsgEnvelope getExchanges(@PathParam("version") String versionString, @PathParam("correction") String correctionString, @QueryParam("id") List<String> bundleStrings) {
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionString, correctionString);
    final List<ExternalId> externalIds = new ArrayList<ExternalId>(bundleStrings.size());
    for (String externalId : bundleStrings) {
      externalIds.add(ExternalId.parse(externalId));
    }
    final ExternalIdBundle bundle = ExternalIdBundle.of(externalIds);
    final Collection<? extends Exchange> exchanges = getUnderlying().getExchanges(bundle, versionCorrection);
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg response = serializer.newMessage();
    for (Exchange exchange : exchanges) {
      serializer.addToMessageWithClassHeaders(response, "exchange", null, exchange);
    }
    return new FudgeMsgEnvelope(response);
  }

  @GET
  @Path("exchange")
  public FudgeMsgEnvelope getExchange(@QueryParam("id") List<String> bundleStrings) {
    final List<ExternalId> externalIds = new ArrayList<ExternalId>(bundleStrings.size());
    for (String externalId : bundleStrings) {
      externalIds.add(ExternalId.parse(externalId));
    }
    final ExternalIdBundle bundle = ExternalIdBundle.of(externalIds);
    return serializeExchange(getUnderlying().getSingleExchange(bundle));
  }

}
