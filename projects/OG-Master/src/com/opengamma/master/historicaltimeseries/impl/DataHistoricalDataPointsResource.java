/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * RESTful resource for accessing time-series data points.
 */
@Path("/htsMaster/dataPoints/{dpId}")
public class DataHistoricalDataPointsResource extends AbstractDataResource {

  /**
   * The parent resource.
   */
  private final DataHistoricalTimeSeriesMasterResource _htsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param htsResource  the parent resource, not null
   * @param dpId  the data-points unique identifier, not null
   */
  public DataHistoricalDataPointsResource(final DataHistoricalTimeSeriesMasterResource htsResource, final ObjectId dpId) {
    ArgumentChecker.notNull(htsResource, "htsResource");
    ArgumentChecker.notNull(dpId, "dpId");
    _htsResource = htsResource;
    _urlResourceId = dpId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent resource.
   * 
   * @return the parent resource, not null
   */
  public DataHistoricalTimeSeriesMasterResource getParentResource() {
    return _htsResource;
  }

  /**
   * Gets the data-points identifier from the URL.
   * 
   * @return the object identifier, not null
   */
  public ObjectId getUrlDataPointsId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series master.
   * 
   * @return the time-series master, not null
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return getParentResource().getHistoricalTimeSeriesMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo, @QueryParam("filter") HistoricalTimeSeriesGetFilter filter) {
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    VersionCorrection vc = VersionCorrection.of(v, c);
    if (filter != null) {
      ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId(), vc, filter);
      return Response.ok(result).build();
    } else {
      ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId(), vc);
      return Response.ok(result).build();
    }
  }

  @POST
  @Path("updates")
  @Consumes(FudgeRest.MEDIA)
  public Response postUpdates(LocalDateDoubleTimeSeries newPoints) {
    UniqueId result = getHistoricalTimeSeriesMaster().updateTimeSeriesDataPoints(getUrlDataPointsId(), newPoints);
    return Response.ok(result).build();
  }

  @POST
  @Path("corrections")
  @Consumes(FudgeRest.MEDIA)
  public Response postCorrections(LocalDateDoubleTimeSeries newPoints) {
    UniqueId result = getHistoricalTimeSeriesMaster().correctTimeSeriesDataPoints(getUrlDataPointsId(), newPoints);
    return Response.ok(result).build();
  }

  @DELETE
  @Path("removals/{startDate}/{endDate}")
  @Consumes(FudgeRest.MEDIA)
  public Response remove(@PathParam("startDate") String startDateStr, @PathParam("endDate") String endDateStr) {
    LocalDate fromDateInclusive = (startDateStr != null ? LocalDate.parse(startDateStr) : null);
    LocalDate toDateInclusive = (endDateStr != null ? LocalDate.parse(endDateStr) : null);
    
    UniqueId result = getHistoricalTimeSeriesMaster().removeTimeSeriesDataPoints(getUrlDataPointsId(), fromDateInclusive, toDateInclusive);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId, @QueryParam("filter") HistoricalTimeSeriesGetFilter filter) {
    if (filter != null) {
      ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId().atVersion(versionId), filter);
      return Response.ok(result).build();
    } else {
      ManageableHistoricalTimeSeries result = getHistoricalTimeSeriesMaster().getTimeSeries(getUrlDataPointsId().atVersion(versionId));
      return Response.ok(result).build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param versionCorrection  the version-correction locator, null for latest
   * @param filterMsg  the filter message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection versionCorrection, String filterMsg) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}");
    if (versionCorrection != null && versionCorrection.getVersionAsOf() != null) {
      b.queryParam("versionAsOf", versionCorrection.getVersionAsOf());
    }
    if (versionCorrection != null && versionCorrection.getCorrectedTo() != null) {
      b.queryParam("correctedTo", versionCorrection.getCorrectedTo());
    }
    if (filterMsg != null) {
      b.queryParam("filter", filterMsg);
    }
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource unique identifier, not null
   * @param filterMsg  the filter message, may be null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId, String filterMsg) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/versions/{versionId}");
    if (filterMsg != null) {
      b.queryParam("filter", filterMsg);
    }
    return b.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

  /**
   * Builds a URI for the corrections resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uriUpdates(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/updates");
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the corrections resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uriCorrections(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/corrections");
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the corrections resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param fromDateInclusive  the start date, may be null
   * @param toDateInclusive  the end date, may be null
   * @return the URI, not null
   */
  public static URI uriRemovals(URI baseUri, ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/dataPoints/{dpId}/removals/{startDate}/{endDate}");
    return b.build(objectId.getObjectId(), ObjectUtils.toString(fromDateInclusive, ""), ObjectUtils.toString(toDateInclusive, ""));
  }

}
