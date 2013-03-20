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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.id.VersionCorrection;
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
   * @param source the underlying source, not null
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
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("currencyMatrices/{name}/{versionCorrection}")
  public Response getMatrix(@PathParam("name") String name, @PathParam("versionCorrection") String versionCorrectionStr) {
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
    CurrencyMatrix result = getCurrencyMatrixSource().getCurrencyMatrix(name, versionCorrection);
    return responseOkFudge(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param name the name, not null
   * @param versionCorrection the version/correction timestamp
   * @return the URI, not null
   */
  public static URI uriGetMatrix(URI baseUri, String name, VersionCorrection versionCorrection) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/currencyMatrices/{name}/{versionCorrection}");
    versionCorrection = versionCorrection != null ? versionCorrection : VersionCorrection.LATEST;
    return bld.build(name, versionCorrection);
  }

}
