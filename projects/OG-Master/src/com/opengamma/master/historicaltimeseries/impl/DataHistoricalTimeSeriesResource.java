/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for accessing time-series info.
 */
@Path("/htsMaster/infos/{infoId}")
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
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().get(getUrlInfoId(), VersionCorrection.of(v, c));
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(HistoricalTimeSeriesInfoDocument request) {
    if (getUrlInfoId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getHistoricalTimeSeriesMaster().remove(getUrlInfoId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    HistoricalTimeSeriesInfoHistoryRequest request = decodeBean(HistoricalTimeSeriesInfoHistoryRequest.class, providers, msgBase64);
    if (getUrlInfoId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HistoricalTimeSeriesInfoHistoryResult result = getHistoricalTimeSeriesMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    HistoricalTimeSeriesInfoDocument result = getHistoricalTimeSeriesMaster().get(getUrlInfoId().atVersion(versionId));
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param versionCorrection  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/infos/{infoId}");
    if (versionCorrection != null && versionCorrection.getVersionAsOf() != null) {
      b.queryParam("versionAsOf", versionCorrection.getVersionAsOf());
    }
    if (versionCorrection != null && versionCorrection.getCorrectedTo() != null) {
      b.queryParam("correctedTo", versionCorrection.getCorrectedTo());
    }
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/infos/{infoId}/versions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/infos/{infoId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
