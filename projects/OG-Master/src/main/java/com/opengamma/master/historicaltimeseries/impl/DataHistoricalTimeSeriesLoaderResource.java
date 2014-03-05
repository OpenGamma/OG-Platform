/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the time-series loader.
 * <p>
 * This resource receives and processes RESTful calls to the time-series loader.
 */
@Path("htsLoader")
public class DataHistoricalTimeSeriesLoaderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final HistoricalTimeSeriesLoader _historicalTimeSeriesLoader;

  /**
   * Creates the resource, exposing the underlying loader over REST.
   * 
   * @param historicalTimeSeriesLoader  the underlying loader, not null
   */
  public DataHistoricalTimeSeriesLoaderResource(final HistoricalTimeSeriesLoader historicalTimeSeriesLoader) {
    ArgumentChecker.notNull(historicalTimeSeriesLoader, "historicalTimeSeriesLoader");
    _historicalTimeSeriesLoader = historicalTimeSeriesLoader;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying time-series loader.
   * 
   * @return the underlying time-series loader, not null
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader() {
    return _historicalTimeSeriesLoader;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("htsLoad")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("htsLoad")
  public Response loadTimeSeries(HistoricalTimeSeriesLoaderRequest request) {
    HistoricalTimeSeriesLoaderResult result = getHistoricalTimeSeriesLoader().loadTimeSeries(request);
    return responseOkObject(result);
  }

  @POST
  @Path("htsUpdate/{uniqueId}")
  public Response updateTimeSeries(@PathParam("uniqueId") String uniqueId) {
    boolean succes = getHistoricalTimeSeriesLoader().updateTimeSeries(UniqueId.parse(uniqueId));
    return responseOkObject(succes);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsLoad");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriUpdate(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsUpdate/{uniqueId}");
    return bld.build(uniqueId);
  }

}
