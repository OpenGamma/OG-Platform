/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

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
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for a snapshot.
 */
@Path("/snpMaster/snapshots/{snapshotId}")
public class DataMarketDataSnapshotResource extends AbstractDataResource {

  /**
   * The snapshots resource.
   */
  private final DataMarketDataSnapshotsResource _snapshotsResource;
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
  public DataMarketDataSnapshotResource(final DataMarketDataSnapshotsResource snapshotsResource, final ObjectId snapshotId) {
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
  public DataMarketDataSnapshotsResource getMarketDataSnapshotsResource() {
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
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().get(getUrlMarketDataSnapshotId(), VersionCorrection.of(v, c));
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(MarketDataSnapshotDocument request) {
    if (getUrlMarketDataSnapshotId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getMarketDataSnapshotMaster().remove(getUrlMarketDataSnapshotId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    MarketDataSnapshotHistoryRequest request = decodeBean(MarketDataSnapshotHistoryRequest.class, providers, msgBase64);
    if (getUrlMarketDataSnapshotId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    MarketDataSnapshotHistoryResult result = getMarketDataSnapshotMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().get(getUrlMarketDataSnapshotId().atVersion(versionId));
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
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/snapshots/{snapshotId}");
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/snapshots/{snapshotId}/versions");
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
    return UriBuilder.fromUri(baseUri).path("/snapshots/{snapshotId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
