/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

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
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a snapshot.
 */
public class DataMarketDataSnapshotResource extends AbstractDataResource {

  /**
   * The snapshots resource.
   */
  private final DataMarketDataSnapshotMasterResource _snapshotsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param snapshotsResource  the parent resource, not null
   * @param snapshotId  the snapshot unique identifier, not null
   */
  public DataMarketDataSnapshotResource(final DataMarketDataSnapshotMasterResource snapshotsResource, final ObjectId snapshotId) {
    ArgumentChecker.notNull(snapshotsResource, "snapshotsResource");
    ArgumentChecker.notNull(snapshotId, "snapshot");
    _snapshotsResource = snapshotsResource;
    _urlResourceId = snapshotId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshots resource.
   * 
   * @return the snapshots resource, not null
   */
  public DataMarketDataSnapshotMasterResource getMarketDataSnapshotsResource() {
    return _snapshotsResource;
  }

  /**
   * Gets the snapshot identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlMarketDataSnapshotId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot master.
   * 
   * @return the snapshot master, not null
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return getMarketDataSnapshotsResource().getMarketDataSnapshotMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().get(getUrlMarketDataSnapshotId(), vc);
    return responseOkFudge(result);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, MarketDataSnapshotDocument request) {
    if (getUrlMarketDataSnapshotId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  @DELETE
  public void remove() {
    getMarketDataSnapshotMaster().remove(getUrlMarketDataSnapshotId().atLatestVersion());
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    MarketDataSnapshotHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, MarketDataSnapshotHistoryRequest.class);
    if (getUrlMarketDataSnapshotId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    MarketDataSnapshotHistoryResult result = getMarketDataSnapshotMaster().history(request);
    return responseOkFudge(result);
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlMarketDataSnapshotId().atVersion(versionId);
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().get(uniqueId);
    return responseOkFudge(result);
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, MarketDataSnapshotDocument request) {
    UniqueId uniqueId = getUrlMarketDataSnapshotId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/snapshots/{snapshotId}");
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
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, MarketDataSnapshotHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/snapshots/{snapshotId}/versions");
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/snapshots/{snapshotId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
