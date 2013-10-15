/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

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
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserHistoryRequest;
import com.opengamma.master.user.UserHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for an user.
 */
public class DataUserResource extends AbstractDocumentDataResource<UserDocument> {

  /**
   * The users resource.
   */
  private final DataUserMasterResource _usersResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataUserResource() {
    _usersResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param usersResource  the parent resource, not null
   * @param userId  the user unique identifier, not null
   */
  public DataUserResource(final DataUserMasterResource usersResource, final ObjectId userId) {
    ArgumentChecker.notNull(usersResource, "usersResource");
    ArgumentChecker.notNull(userId, "user");
    _usersResource = usersResource;
    _urlResourceId = userId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the users resource.
   *
   * @return the users resource, not null
   */
  public DataUserMasterResource getUsersResource() {
    return _usersResource;
  }

  /**
   * Gets the user identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user master.
   *
   * @return the user master, not null
   */
  public UserMaster getMaster() {
    return getUsersResource().getUserMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    UserHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, UserHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    UserHistoryResult result = getMaster().history(request);
    return responseOkFudge(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, UserDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<UserDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<UserDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<UserDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "users";
  }

}
