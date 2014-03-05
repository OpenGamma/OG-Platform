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

import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for time-series.
 * <p>
 * The time-series resource receives and processes RESTful calls to the time-series master.
 */
@Path("htsMaster")
public class DataHistoricalTimeSeriesMasterResource extends AbstractDataResource {

  /**
   * The info master.
   */
  private final HistoricalTimeSeriesMaster _htsMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param infoMaster  the underlying info master, not null
   */
  public DataHistoricalTimeSeriesMasterResource(final HistoricalTimeSeriesMaster infoMaster) {
    ArgumentChecker.notNull(infoMaster, "infoMaster");
    _htsMaster = infoMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the info master.
   *
   * @return the info master, not null
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _htsMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("infos")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @GET
  @Path("metaData")
  public Response metaData(@Context UriInfo uriInfo) {
    HistoricalTimeSeriesInfoMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, HistoricalTimeSeriesInfoMetaDataRequest.class);
    HistoricalTimeSeriesInfoMetaDataResult result = getHistoricalTimeSeriesMaster().metaData(request);
    return responseOkObject(result);
  }

  @POST
  @Path("infoSearches")
  public Response search(HistoricalTimeSeriesInfoSearchRequest request) {
    HistoricalTimeSeriesInfoSearchResult result = getHistoricalTimeSeriesMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("infos")
  public Response add(@Context UriInfo uriInfo, HistoricalTimeSeriesInfoDocument request) {
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().add(request);
    URI createdUri = (new DataHistoricalTimeSeriesResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("infos/{infoId}")
  public DataHistoricalTimeSeriesResource findHistoricalTimeSeries(@PathParam("infoId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataHistoricalTimeSeriesResource(this, id);
  }

  @Path("dataPoints/{dpId}")
  public DataHistoricalDataPointsResource findHistoricalDataPoints(@PathParam("dpId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataHistoricalDataPointsResource(this, id);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for info meta-data.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, HistoricalTimeSeriesInfoMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("infoSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("infos");
    return bld.build();
  }

}
