/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import javax.time.Instant;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterpolatedYieldCurveDefinitionSourceResource {

  private final InterpolatedYieldCurveDefinitionSource _underlying;
  private final FudgeContext _fudgeContext;

  public InterpolatedYieldCurveDefinitionSourceResource(final InterpolatedYieldCurveDefinitionSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected InterpolatedYieldCurveDefinitionSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeMsgEnvelope definitionToMsgEnvelope(final YieldCurveDefinition definition) {
    if (definition == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, "definition", null, definition, YieldCurveDefinition.class);
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("{currency}/{name}")
  public FudgeMsgEnvelope getDefinition(@PathParam("currency") String currencyISO, @PathParam("name") String name) {
    final Currency currency = Currency.of(currencyISO);
    final YieldCurveDefinition definition = getUnderlying().getDefinition(currency, name);
    return definitionToMsgEnvelope(definition);
  }

  @GET
  @Path("{currency}/{name}/{version}")
  public FudgeMsgEnvelope getDefinition(@PathParam("currency") String currencyISO, @PathParam("name") String name, @PathParam("version") long versionMillis) {
    final Currency currency = Currency.of(currencyISO);
    final Instant version = Instant.ofEpochMillis(versionMillis);
    final YieldCurveDefinition definition = getUnderlying().getDefinition(currency, name, version);
    return definitionToMsgEnvelope(definition);
  }

}
