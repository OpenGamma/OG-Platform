/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for {@code AvailableOutputsProvider}.
 */
@Path("availableOutputs")
public class AvailableOutputsService {

  private final AvailableOutputsProvider _provider;
  private final FudgeContext _fudgeContext;

  public AvailableOutputsService(final AvailableOutputsProvider provider, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _provider = provider;
    _fudgeContext = fudgeContext;
  }

  @Path("portfolio")
  public AvailablePortfolioOutputsResource portfolio() {
    return new AvailablePortfolioOutputsResource(_provider, _fudgeContext);
  }

}
