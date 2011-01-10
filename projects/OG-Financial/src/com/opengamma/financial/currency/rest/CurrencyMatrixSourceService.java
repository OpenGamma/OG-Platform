/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteCurrencyMatrixSource}.
 */
@Path("currencyMatrixSource")
public class CurrencyMatrixSourceService extends AbstractResourceService<CurrencyMatrixSource, CurrencyMatrixSourceResource> {

  public CurrencyMatrixSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected CurrencyMatrixSourceResource createResource(CurrencyMatrixSource underlying) {
    return new CurrencyMatrixSourceResource(underlying, getFudgeContext());
  }

}
