/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

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
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for snapshots.
 * <p>
 * The snapshots resource receives and processes RESTful calls to the snapshot master.
 */
@Path("/snpMaster")
public class DataMarketDataSnapshotsResource extends AbstractDataResource {

  /**
   * The snapshot master.
   */
  private final MarketDataSnapshotMaster _snpMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param snapshotMaster  the underlying snapshot master, not null
   */
  public DataMarketDataSnapshotsResource(final MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _snpMaster = snapshotMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot master.
   * 
   * @return the snapshot master, not null
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _snpMaster;
  }

  //-------------------------------------------------------------------------
  @HEAD
  @Path("snapshots")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("snapshots")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    MarketDataSnapshotSearchRequest request = decodeBean(MarketDataSnapshotSearchRequest.class, providers, msgBase64);
    MarketDataSnapshotSearchResult result = getMarketDataSnapshotMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("snapshots")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, MarketDataSnapshotDocument request) {
    MarketDataSnapshotDocument result = getMarketDataSnapshotMaster().add(request);
    URI createdUri = DataMarketDataSnapshotResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("snapshots/{snapshotId}")
  public DataMarketDataSnapshotResource findMarketDataSnapshot(@PathParam("snapshotId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataMarketDataSnapshotResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all snapshots.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/snapshots");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
