/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for accessing time-series info.
 */
public class DataHistoricalTimeSeriesResource extends AbstractDataResource {

  /**
   * The time-series resource.
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
   * @param infoId  the time-series unique identifier, not null
   */
  public DataHistoricalTimeSeriesResource(final DataHistoricalTimeSeriesMasterResource htsResource, final ObjectId infoId) {
    ArgumentChecker.notNull(htsResource, "htsResource");
    ArgumentChecker.notNull(infoId, "infoId");
    _htsResource = htsResource;
    _urlResourceId = infoId;
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
   * Gets the info identifier from the URL.
   * 
   * @return the object identifier, not null
   */
  public ObjectId getUrlInfoId() {
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
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().get(getUrlInfoId(), vc);
    return Response.ok(result).build();
  }

  @POST
  public Response update(@Context UriInfo uriInfo, HistoricalTimeSeriesInfoDocument request) {
    if (getUrlInfoId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  @DELETE
  public Response remove() {
    getHistoricalTimeSeriesMaster().remove(getUrlInfoId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    HistoricalTimeSeriesInfoHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, HistoricalTimeSeriesInfoHistoryRequest.class);
    if (getUrlInfoId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HistoricalTimeSeriesInfoHistoryResult result = getHistoricalTimeSeriesMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlInfoId().atVersion(versionId);
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().get(uniqueId);
    return Response.ok(result).build();
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, HistoricalTimeSeriesInfoDocument request) {
    UniqueId uniqueId = getUrlInfoId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/infos/{infoId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, HistoricalTimeSeriesInfoHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/infos/{infoId}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/infos/{infoId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
