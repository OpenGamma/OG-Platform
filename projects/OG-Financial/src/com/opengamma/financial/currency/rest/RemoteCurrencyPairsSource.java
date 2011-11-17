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

/**
 * Remote source of {@link com.opengamma.financial.currency.CurrencyPairs} backed by a RESTful web service.
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

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public CurrencyPairs getCurrencyPairs(String name) {
    ArgumentChecker.notNull(name, "name");
    return getRestClient().getSingleValue(CurrencyPairs.class, getTargetBase().resolve(name), "currencyPairs");
  }

  /*@Override
  public CurrencyPair getCurrencyPair(Currency currency1, Currency currency2, String name) {
    throw new UnsupportedOperationException("getCurrencyPair not implemented");
  }

  @Override
  public Double getRate(Currency currency1, double amount1, Currency currency2, double amount2, String name) {
    throw new UnsupportedOperationException("getRate not implemented");
  }*/
}
