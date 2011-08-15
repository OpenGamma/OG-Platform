/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.time.Instant;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * REST interface to the AvailableOutputs helper
 */
@Path("availableOutputs")
public class AvailableOutputsService {

  private final CompiledFunctionService _compiledFunctions;
  private final FudgeContext _fudgeContext;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;

  public AvailableOutputsService(final FudgeContext fudgeContext, final CompiledFunctionService compiledFunctionService, final PositionSource positionSource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(compiledFunctionService, "compiledFunctionService");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _fudgeContext = fudgeContext;
    _compiledFunctions = compiledFunctionService;
    _positionSource = positionSource;
    _securitySource = securitySource;
  }

  protected CompiledFunctionService getCompiledFunctions() {
    return _compiledFunctions;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected PositionSource getPositionSource() {
    return _positionSource;
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  protected Portfolio getPortfolio(final String uid) {
    final Portfolio rawPortfolio = getPositionSource().getPortfolio(UniqueId.parse(uid));
    if (rawPortfolio == null) {
      return null;
    }
    return PortfolioCompiler.resolvePortfolio(rawPortfolio, getCompiledFunctions().getExecutorService(), getSecuritySource());
  }

  @Path("now")
  public AvailableOutputsResource now() {
    return new AvailableOutputsResource(this, getCompiledFunctions().compileFunctionRepository(Instant.now()));
  }

  @Path("{timestamp}")
  public AvailableOutputsResource timestamp(@PathParam("timestamp") long timestamp) {
    return new AvailableOutputsResource(this, getCompiledFunctions().compileFunctionRepository(timestamp));
  }

}
