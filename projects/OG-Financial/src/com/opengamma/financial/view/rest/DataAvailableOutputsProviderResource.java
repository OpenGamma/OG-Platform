/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for available outputs.
 * <p>
 * This resource receives and processes RESTful calls.
 */
@Path("availableOutputs")
public class DataAvailableOutputsProviderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final AvailableOutputsProvider _provider;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param provider  the provider, not null
   */
  public DataAvailableOutputsProviderResource(final AvailableOutputsProvider provider) {
    this(provider, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param provider  the provider, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public DataAvailableOutputsProviderResource(final AvailableOutputsProvider provider, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(provider, "provider");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _provider = provider;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the available outputs provider.
   * 
   * @return the available outputs provider, not null
   */
  public AvailableOutputsProvider getAvailableOutputsProvider() {
    return _provider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @Path("portfolio")
  public DataAvailablePortfolioOutputsResource portfolio() {
    return new DataAvailablePortfolioOutputsResource(_provider, _fudgeContext);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriPortfolio(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("portfolio");
    return bld.build();
  }

}
