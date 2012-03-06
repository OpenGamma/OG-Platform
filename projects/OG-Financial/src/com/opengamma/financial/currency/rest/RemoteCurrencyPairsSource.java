/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to a {@link CurrencyPairsSource}.
 */
public class RemoteCurrencyPairsSource extends AbstractRemoteClient implements CurrencyPairsSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteCurrencyPairsSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public CurrencyPairs getCurrencyPairs(String name) {
    if (name == null) {
      name = CurrencyPairs.DEFAULT_CURRENCY_PAIRS;  // TODO: push back to callers
    }
    
    try {
      URI uri = DataCurrencyPairsSourceResource.uriGetPairs(getBaseUri(), name);
      return accessRemote(uri).get(CurrencyPairs.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public CurrencyPair getCurrencyPair(String name, Currency currency1, Currency currency2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    if (name == null) {
      name = CurrencyPairs.DEFAULT_CURRENCY_PAIRS;  // TODO: push back to callers
    }
    
    try {
      URI uri = DataCurrencyPairsSourceResource.uriGetPair(getBaseUri(), name, currency1, currency2);
      return accessRemote(uri).get(CurrencyPair.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

}
