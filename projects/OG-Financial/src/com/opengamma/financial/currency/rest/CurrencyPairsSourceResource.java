/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * RESTful resource that wraps a {@link CurrencyPairsSource}.
 */
public class CurrencyPairsSourceResource {

  private final CurrencyPairsSource _underlying;
  private final FudgeContext _fudgeContext;

  public CurrencyPairsSourceResource(final CurrencyPairsSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected CurrencyPairsSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeMsgEnvelope pairsToMsgEnvelope(final CurrencyPairs pairs) {
    if (pairs == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, "currencyPairs", null, pairs, CurrencyPairs.class);
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("{name}")
  public FudgeMsgEnvelope getDefinition(@PathParam("name") String name) {
    return pairsToMsgEnvelope(getUnderlying().getCurrencyPairs(name));
  }

}
