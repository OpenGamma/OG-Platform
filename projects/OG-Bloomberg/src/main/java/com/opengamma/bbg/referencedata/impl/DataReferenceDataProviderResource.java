/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the reference-data provider.
 * <p>
 * This resource receives and processes RESTful calls to the reference-data provider.
 */
@Path("referenceDataProvider")
public class DataReferenceDataProviderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final ReferenceDataProvider _referenceDataProvider;

  /**
   * Creates the resource, exposing the underlying provider over REST.
   * 
   * @param referenceDataProvider  the underlying provider, not null
   */
  public DataReferenceDataProviderResource(final ReferenceDataProvider referenceDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    _referenceDataProvider = referenceDataProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the reference-data provider.
   * 
   * @return the reference-data provider, not null
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("referenceDataGet")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("referenceDataGet")
  public Response getHistoricalTimeSeries(ReferenceDataProviderGetRequest request) {
    ReferenceDataProviderGetResult result = getReferenceDataProvider().getReferenceData(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("referenceDataGet");
    return bld.build();
  }

}
