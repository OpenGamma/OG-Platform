/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.money.Currency;
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
 * TODO what do I need to configure to be able to use Currency instances in the methods?
 */
public class CurrencyPairsResource {

  private final CurrencyPairs _currencyPairs;
  private final FudgeContext _fudgeContext;

  CurrencyPairsResource(CurrencyPairs currencyPairs, FudgeContext fudgeContext) {
    _currencyPairs = currencyPairs;
    _fudgeContext = fudgeContext;
  }

  @GET
  @Path("currencyPairs")
  public FudgeMsgEnvelope getAllCurrencyPairs() {
    FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, "currencyPairs", null, _currencyPairs, CurrencyPairs.class);
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("{currency1}/{currrency2}")
  public FudgeMsgEnvelope getCurrencyPair(@PathParam("currency1") String currency1, @PathParam("currrency2") String currency2) {
  /*public FudgeMsgEnvelope getCurrencyPair(@PathParam("currency1") Currency currency1, @PathParam("currrency2") Currency currency2) {
    CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(currency1, currency2);*/
    CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(Currency.of(currency1), Currency.of(currency2));
    if (currencyPair == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, "currencyPair", null, currencyPair, CurrencyPair.class);
    return new FudgeMsgEnvelope(msg);
  }
}
