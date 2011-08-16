/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.time.Instant;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailablePortfolioOutputs;
import com.opengamma.transport.jaxrs.FudgeFieldContainerBrowser;

/**
 * REST interface to the AvailablePortfolioOutputs helper
 */
public class AvailablePortfolioOutputsResource {

  private final AvailableOutputsService _service;

  /**
   * 
   */
  public static final class Instance {

    private final AvailableOutputsService _service;
    private final CompiledFunctionRepository _functions;

    private Instance(final AvailableOutputsService service, final CompiledFunctionRepository functions) {
      _service = service;
      _functions = functions;
    }

    @Path("{portfolio}")
    public FudgeFieldContainerBrowser portfolioOutputs(@PathParam("portfolio") String portfolioUid) {
      final Portfolio portfolio = _service.getPortfolio(portfolioUid);
      if (portfolio == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      final AvailableOutputs outputs = new AvailablePortfolioOutputs(portfolio, _functions, _service.getWildcardIndicator());
      final FudgeSerializer fudgeContext = new FudgeSerializer(_service.getFudgeContext());
      return new FudgeFieldContainerBrowser(fudgeContext.objectToFudgeMsg(outputs));
    }

  }

  public AvailablePortfolioOutputsResource(final AvailableOutputsService service) {
    _service = service;
  }

  protected AvailableOutputsService getService() {
    return _service;
  }

  @Path("now")
  public Instance now() {
    return new Instance(getService(), getService().getCompiledFunctions().compileFunctionRepository(Instant.now()));
  }

  @Path("{timestamp}")
  public Instance timestamp(@PathParam("timestamp") long timestamp) {
    return new Instance(getService(), getService().getCompiledFunctions().compileFunctionRepository(timestamp));
  }

}
