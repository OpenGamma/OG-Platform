/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for users.
 * The users resource receives and processes RESTful calls to the user master.
 */
@Path("userMaster")
public class DataUserMasterResource extends AbstractDataResource {

  /**
   * The user master.
   */
  private final UserMaster _userMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param userMaster  the underlying user master, not null
   */
  public DataUserMasterResource(final UserMaster userMaster) {
    ArgumentChecker.notNull(userMaster, "userMaster");
    _userMaster = userMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the user master.
   *
   * @return the user master, not null
   */
  public UserMaster getUserMaster() {
    return _userMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("users")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("userSearches")
  public Response search(UserSearchRequest request) {
    UserSearchResult result = getUserMaster().search(request);
    return responseOkFudge(result);
  }

  @POST
  @Path("users")
  public Response add(@Context UriInfo uriInfo, UserDocument request) {
    UserDocument result = getUserMaster().add(request);
    URI createdUri = (new DataUserResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(createdUri, result);
  }

  @Path("users/{userId}")
  public DataUserResource findUser(@PathParam("userId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataUserResource(this, id);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("userSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users");
    return bld.build();
  }

}
