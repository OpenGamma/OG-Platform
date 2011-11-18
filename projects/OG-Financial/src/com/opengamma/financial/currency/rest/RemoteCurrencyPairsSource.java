/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import org.fudgemsg.FudgeContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Remote source of {@link CurrencyPairs} backed by a RESTful web service.
 */
public class RemoteCurrencyPairsSource implements CurrencyPairsSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteCurrencyPairsSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  @Override
  public CurrencyPairs getCurrencyPairs(String name) {
    ArgumentChecker.notNull(name, "name");
    // invoke {name}/currencyPairs
    return _restClient.getSingleValue(CurrencyPairs.class, _targetBase.resolveBase(name).resolve("currencyPairs"), "currencyPairs");
  }

  @Override
  public CurrencyPair getCurrencyPair(Currency currency1, Currency currency2, String name) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    ArgumentChecker.notNull(name, "name");
    // invoke {name}/{currency1}/{currency2}
    return _restClient.getSingleValue(CurrencyPair.class, _targetBase
        .resolveBase(name)
        .resolveBase(currency1.getCode())
        .resolve(currency2.getCode()),
        "currencyPair");
  }
}
