/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

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
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a position in the position master.
 */
public class DataPositionResource extends AbstractDocumentDataResource<PositionDocument> {

  /**
   * The parent resource.
   */
  private final DataPositionMasterResource _parentResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataPositionResource() {
    _parentResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param parentResource  the parent resource, not null
   * @param positionId  the position unique identifier, not null
   */
  public DataPositionResource(final DataPositionMasterResource parentResource, final ObjectId positionId) {
    ArgumentChecker.notNull(parentResource, "parentResource");
    ArgumentChecker.notNull(positionId, "position");
    _parentResource = parentResource;
    _urlResourceId = positionId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the parent resource.
   *
   * @return the parent resource, not null
   */
  public DataPositionMasterResource getParentResource() {
    return _parentResource;
  }

  /**
   * Gets the position identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the position master.
   *
   * @return the position master, not null
   */
  public PositionMaster getMaster() {
    return getParentResource().getPositionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    PositionHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, PositionHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PositionHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, PositionDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<PositionDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<PositionDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<PositionDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "positions";
  }
}
