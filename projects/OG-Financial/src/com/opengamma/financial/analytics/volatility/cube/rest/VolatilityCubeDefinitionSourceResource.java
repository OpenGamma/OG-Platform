/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import javax.time.Instant;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class VolatilityCubeDefinitionSourceResource {
  private final VolatilityCubeDefinitionSource _underlying;
  private final FudgeContext _fudgeContext;

  public VolatilityCubeDefinitionSourceResource(final VolatilityCubeDefinitionSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected VolatilityCubeDefinitionSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeMsgEnvelope definitionToMsgEnvelope(final VolatilityCubeDefinition definition) {
    if (definition == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeMsg msg = context.newMessage();
    context.addToMessageWithClassHeaders(msg, "definition", null, definition, VolatilityCubeDefinition.class);
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("{currency}/{name}")
  public FudgeMsgEnvelope getDefinition(@PathParam("currency") String currencyISO, @PathParam("name") String name) {
    final Currency currency = Currency.of(currencyISO);
    final VolatilityCubeDefinition definition = getUnderlying().getDefinition(currency, name);
    return definitionToMsgEnvelope(definition);
  }

  @GET
  @Path("{currency}/{name}/{version}")
  public FudgeMsgEnvelope getDefinition(@PathParam("currency") String currencyISO, @PathParam("name") String name, @PathParam("version") long versionMillis) {
    final Currency currency = Currency.of(currencyISO);
    final Instant version = Instant.ofEpochMillis(versionMillis);
    final VolatilityCubeDefinition definition = getUnderlying().getDefinition(currency, name, version);
    return definitionToMsgEnvelope(definition);
  }

}
