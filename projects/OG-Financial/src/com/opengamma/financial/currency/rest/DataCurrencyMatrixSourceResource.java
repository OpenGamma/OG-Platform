/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for currency matrices.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("/currencyMatrixSource")
public class DataCurrencyMatrixSourceResource extends AbstractDataResource {

  /**
   * The source.
   */
  private final CurrencyMatrixSource _source;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param source  the underlying source, not null
   */
  public DataCurrencyMatrixSourceResource(final CurrencyMatrixSource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source.
   * 
   * @return the source, not null
   */
  public CurrencyMatrixSource getCurrencyMatrixSource() {
    return _source;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("currencyMatrices/{name}")
  public Response getMatrix(@PathParam("name") String name) {
    CurrencyMatrix result = getCurrencyMatrixSource().getCurrencyMatrix(name);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param name  the name, not null
   * @return the URI, not null
   */
  public static URI uriGetMatrix(URI baseUri, String name) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyMatrices/{name}");
    return bld.build(name);
  }

}
