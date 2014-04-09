/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for users.
 * <p>
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
    return responseOkObject(result);
  }

  @POST
  @Path("users")
  public Response add(@Context UriInfo uriInfo, ManageableUser user) {
    UniqueId result = getUserMaster().add(user);
    URI createdUri = uriUserById(uriInfo.getBaseUri(), result);
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("users/{objectId}")
  public Response getById(@PathParam("objectId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    ManageableUser result = getUserMaster().getById(id);
    return responseOkObject(result);
  }

  @PUT
  @Path("users/{objectId}")
  public Response updateById(@Context UriInfo uriInfo, @PathParam("objectId") String idStr, ManageableUser user) {
    ObjectId id = ObjectId.parse(idStr);
    if (id.equals(user.getObjectId()) == false) {
      throw new IllegalArgumentException("ObjectId of user does not match URI");
    }
    UniqueId result = getUserMaster().update(user);
    return responseOkObject(result);
  }

  @DELETE
  @Path("users/{objectId}")
  public void removeById(@PathParam("objectId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    getUserMaster().removeById(id);
  }

  @GET
  @Path("users/{objectId}/eventHistory")
  public Response eventHistoryById(@PathParam("objectId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    UserEventHistoryRequest request = new UserEventHistoryRequest(id);
    UserEventHistoryResult result = getUserMaster().eventHistory(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("users/exists/{userName}")
  public Response nameExists(@PathParam("userName") String userName) {
    boolean exists = getUserMaster().nameExists(userName);
    return (exists ? responseOk() : Response.status(Status.NOT_FOUND).build());
  }

  @GET
  @Path("users/name/{userName}")
  public Response getByName(@PathParam("userName") String userName) {
    ManageableUser result = getUserMaster().getByName(userName);
    return responseOkObject(result);
  }

  @PUT
  @Path("users/name/{userName}")
  public Response updateByName(@Context UriInfo uriInfo, @PathParam("userName") String userName, ManageableUser user) {
    ManageableUser current = getUserMaster().getByName(userName);
    if (current.getObjectId().equals(user.getObjectId()) == false) {
      throw new IllegalArgumentException("User does not match URI");
    }
    UniqueId result = getUserMaster().update(user);
    return responseOkObject(result);
  }

  @DELETE
  @Path("users/name/{userName}")
  public void removeByName(@PathParam("userName") String userName) {
    getUserMaster().removeByName(userName);
  }

  @GET
  @Path("users/name/{userName}/eventHistory")
  public Response eventHistoryByName(@PathParam("userName") String userName) {
    UserEventHistoryRequest request = new UserEventHistoryRequest(userName);
    UserEventHistoryResult result = getUserMaster().eventHistory(request);
    return responseOkObject(result);
  }

  @GET
  @Path("users/name/{userName}/account")
  public Response accountByName(@PathParam("userName") String userName) {
    UserAccount account = getUserMaster().getAccount(userName);
    return responseOkObject(account);
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

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @return the URI, not null
   */
  public static URI uriUserById(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/{objectId}");
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriUserByName(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/name/{userName}");
    return bld.build(userName);
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriNameExists(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/exists/{userName}");
    return bld.build(userName);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, not null
   * @return the URI, not null
   */
  public static URI uriEventHistory(URI baseUri, UserEventHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri);
    if (request.getObjectId() != null) {
      return bld.path("/users/{objectId}/eventHistory").build(request.getObjectId());
    } else {
      return bld.path("/users/name/{userName}/eventHistory").build(request.getUserName());
    }
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriUserAccountByName(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/users/name/{userName}/account");
    return bld.build(userName);
  }

}
