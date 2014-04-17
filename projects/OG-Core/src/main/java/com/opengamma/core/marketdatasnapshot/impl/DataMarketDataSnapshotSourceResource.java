/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for snapshots.
 * <p>
 * The snapshots resource receives and processes RESTful calls to the snapshot source.
 */
@Path("snapshotSource")
public class DataMarketDataSnapshotSourceResource extends AbstractDataResource {

  /**
   * The snapshot source.
   */
  private final MarketDataSnapshotSource _snpSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param snapshotSource  the underlying snapshot source, not null
   */
  public DataMarketDataSnapshotSourceResource(final MarketDataSnapshotSource snapshotSource) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    _snpSource = snapshotSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot source.
   * 
   * @return the snapshot source, not null
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource() {
    return _snpSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("snapshots/{snapshotId}")
  public Response get(
      @PathParam("snapshotId") String idStr,
      @QueryParam("version") String version) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final StructuredMarketDataSnapshot result = getMarketDataSnapshotSource().get(objectId.atVersion(version));
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots/{snapshotId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots/{snapshotId}");
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param vc  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("snapshots/{snapshotId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }
}
