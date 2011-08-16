/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.transport.jaxrs.FudgeFieldContainerBrowser;

/**
 * REST interface to the AvailableOutputs helper
 */
public class AvailableOutputsResource {

  private final AvailableOutputsService _service;
  private final CompiledFunctionRepository _compiledFunctions;

  public AvailableOutputsResource(final AvailableOutputsService service, final CompiledFunctionRepository compiledFunctions) {
    _service = service;
    _compiledFunctions = compiledFunctions;
  }

  protected AvailableOutputsService getService() {
    return _service;
  }

  protected CompiledFunctionRepository getCompiledFunctions() {
    return _compiledFunctions;
  }

  @Path("{portfolio}")
  public FudgeFieldContainerBrowser portfolioOutputs(@PathParam("portfolio") String portfolioUid) {
    final FudgeSerializer fudgeContext = new FudgeSerializer(getService().getFudgeContext());
    final MutableFudgeMsg msg = fudgeContext.newMessage();
    final Portfolio portfolio = getService().getPortfolio(portfolioUid);
    if (portfolio == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final AvailableOutputs outputs = new AvailableOutputs(portfolio, getCompiledFunctions(), getService().getWildcardIndicator());
    MutableFudgeMsg submsg = fudgeContext.newMessage();
    for (AvailableOutput output : outputs.getPortfolioNodeOutputs()) {
      fudgeContext.addToMessage(submsg, output.getValueName(), null, output.getPortfolioNodeProperties());
    }
    msg.add(null, null, submsg);
    for (String securityType : outputs.getSecurityTypes()) {
      submsg = fudgeContext.newMessage();
      for (AvailableOutput output : outputs.getPositionOutputs(securityType)) {
        fudgeContext.addToMessage(submsg, output.getValueName(), null, output.getPositionProperties(securityType));
      }
      msg.add(securityType, null, submsg);
    }
    return new FudgeFieldContainerBrowser(msg);
  }

}
