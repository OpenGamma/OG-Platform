/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.util.rest.AbstractResourceService;
import org.fudgemsg.FudgeContext;

import javax.ws.rs.Path;

/**
 * RESTful backend for {@link com.opengamma.financial.currency.rest.RemoteCurrencyPairsSource}.
 */
@Path("currencyPairsSource")
public class CurrencyPairsSourceService extends AbstractResourceService<CurrencyPairsSource, CurrencyPairsSourceResource> {

  public CurrencyPairsSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected CurrencyPairsSourceResource createResource(CurrencyPairsSource underlying) {
    return new CurrencyPairsSourceResource(underlying, getFudgeContext());
  }

}
