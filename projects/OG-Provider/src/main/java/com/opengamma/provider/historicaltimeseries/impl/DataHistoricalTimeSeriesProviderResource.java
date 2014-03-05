/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the time-series provider.
 * <p>
 * This resource receives and processes RESTful calls to the time-series provider.
 */
@Path("htsProvider")
public class DataHistoricalTimeSeriesProviderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final HistoricalTimeSeriesProvider _htsProvider;

  /**
   * Creates the resource, exposing the underlying provider over REST.
   * 
   * @param htsProvider  the underlying provider, not null
   */
  public DataHistoricalTimeSeriesProviderResource(final HistoricalTimeSeriesProvider htsProvider) {
    ArgumentChecker.notNull(htsProvider, "htsProvider");
    _htsProvider = htsProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series provider.
   * 
   * @return the time-series provider, not null
   */
  public HistoricalTimeSeriesProvider getHistoricalTimeSeriesProvider() {
    return _htsProvider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("htsGet")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("htsGet")
  public Response getHistoricalTimeSeries(HistoricalTimeSeriesProviderGetRequest request) {
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeriesProvider().getHistoricalTimeSeries(request);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsGet");
    return bld.build();
  }

}
