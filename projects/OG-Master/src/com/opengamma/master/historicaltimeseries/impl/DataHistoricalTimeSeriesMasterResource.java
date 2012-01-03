/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for time-series.
 * <p>
 * The time-series resource receives and processes RESTful calls to the time-series master.
 */
@Path("/htsMaster")
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
  @Path("metaData")
  public Response metaData(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    HistoricalTimeSeriesInfoMetaDataRequest request = new HistoricalTimeSeriesInfoMetaDataRequest();
    if (msgBase64 != null) {
      request = decodeBean(HistoricalTimeSeriesInfoMetaDataRequest.class, providers, msgBase64);
    }
    HistoricalTimeSeriesInfoMetaDataResult result = getHistoricalTimeSeriesMaster().metaData(request);
    return Response.ok(result).build();
  }

  @HEAD
  @Path("infos")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("infos")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    HistoricalTimeSeriesInfoSearchRequest request = decodeBean(HistoricalTimeSeriesInfoSearchRequest.class, providers, msgBase64);
    HistoricalTimeSeriesInfoSearchResult result = getHistoricalTimeSeriesMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("infos")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, HistoricalTimeSeriesInfoDocument request) {
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().add(request);
    URI createdUri = DataHistoricalTimeSeriesResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
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
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/metaData");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

  /**
   * Builds a URI for infos.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/infos");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
