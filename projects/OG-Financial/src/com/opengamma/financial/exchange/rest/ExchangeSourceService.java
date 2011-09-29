/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.exchange.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteExchangeSource}.
 */
@Path("exchangeSource")
public class ExchangeSourceService extends AbstractResourceService<ExchangeSource, ExchangeSourceResource> {

  public ExchangeSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected ExchangeSourceResource createResource(ExchangeSource underlying) {
    return new ExchangeSourceResource(underlying, getFudgeContext());
  }

}
