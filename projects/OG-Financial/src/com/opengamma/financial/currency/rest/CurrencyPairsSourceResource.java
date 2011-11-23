/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import org.fudgemsg.FudgeContext;

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

  /**
   * @param name The name of a set of market convention currency pairs
   * @return A REST sub-resource representing a set of currency pairs
   * @throws WebApplicationException Status 404 if name doesn't correspond to a set of currency pairs in the system
   */
  @Path("{name}")
  public CurrencyPairsResource getPairs(@PathParam("name") String name) {
    CurrencyPairs currencyPairs = _underlying.getCurrencyPairs(name);
    if (currencyPairs == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return new CurrencyPairsResource(currencyPairs, _fudgeContext);
  }

}
