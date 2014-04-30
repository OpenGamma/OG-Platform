/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a snapshot.
 */
public class DataMarketDataSnapshotResource extends AbstractDocumentDataResource<MarketDataSnapshotDocument> {

  /**
   * The snapshots resource.
   */
  private final DataMarketDataSnapshotMasterResource _snapshotsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataMarketDataSnapshotResource() {
    _snapshotsResource = null;
  }

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
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the snapshot master.
   *
   * @return the snapshot master, not null
   */
  public MarketDataSnapshotMaster getMaster() {
    return getMarketDataSnapshotsResource().getMarketDataSnapshotMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    MarketDataSnapshotHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, MarketDataSnapshotHistoryRequest.class);
    if (request.getObjectId() != null) {
      if (!request.getObjectId().equals(getUrlId())) {
        throw new IllegalArgumentException("Document objectId " + request.getObjectId() + " does not match URI " + getUrlId());
      }
    } else {
      request.setObjectId(getUrlId());
    }
    MarketDataSnapshotHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, MarketDataSnapshotDocument request) {
    return super.update(uriInfo, request);
  }

  @DELETE
  public void remove() {
    super.remove();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    return super.getVersioned(versionId);
  }


  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") String versionId, List<MarketDataSnapshotDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<MarketDataSnapshotDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<MarketDataSnapshotDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "snapshots";
  }

}
