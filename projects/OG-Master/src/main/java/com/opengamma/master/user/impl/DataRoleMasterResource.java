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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for roles.
 * <p>
 * The roles resource receives and processes RESTful calls to the role master.
 */
@Path("roleMaster")
public class DataRoleMasterResource extends AbstractDataResource {

  /**
   * The role master.
   */
  private final RoleMaster _roleMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param roleMaster  the underlying role master, not null
   */
  public DataRoleMasterResource(final RoleMaster roleMaster) {
    ArgumentChecker.notNull(roleMaster, "roleMaster");
    _roleMaster = roleMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the role master.
   *
   * @return the role master, not null
   */
  public RoleMaster getRoleMaster() {
    return _roleMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("roles")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("roleSearches")
  public Response search(RoleSearchRequest request) {
    RoleSearchResult result = getRoleMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("roles")
  public Response add(@Context UriInfo uriInfo, ManageableRole role) {
    UniqueId result = getRoleMaster().add(role);
    URI createdUri = uriRoleById(uriInfo.getBaseUri(), result);
    return responseCreated(createdUri);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("roles/{objectId}")
  public Response getById(@PathParam("objectId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    ManageableRole result = getRoleMaster().getById(id);
    return responseOkObject(result);
  }

  @PUT
  @Path("roles/{objectId}")
  public Response updateById(@Context UriInfo uriInfo, @PathParam("objectId") String idStr, ManageableRole role) {
    ObjectId id = ObjectId.parse(idStr);
    if (id.equals(role.getObjectId()) == false) {
      throw new IllegalArgumentException("ObjectId of role does not match URI");
    }
    UniqueId result = getRoleMaster().update(role);
    return responseOkObject(result);
  }

  @DELETE
  @Path("roles/{objectId}")
  public void removeById(@PathParam("objectId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    getRoleMaster().removeById(id);
  }

  @GET
  @Path("roles/{objectId}/eventHistory")
  public Response eventHistoryById(@PathParam("objectId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    RoleEventHistoryRequest request = new RoleEventHistoryRequest(id);
    RoleEventHistoryResult result = getRoleMaster().eventHistory(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("roles/exists/{roleName}")
  public Response nameExists(@PathParam("roleName") String roleName) {
    boolean exists = getRoleMaster().nameExists(roleName);
    return (exists ? responseOk() : Response.status(Status.NOT_FOUND).build());
  }

  @GET
  @Path("roles/name/{roleName}")
  public Response getByName(@PathParam("roleName") String roleName) {
    ManageableRole result = getRoleMaster().getByName(roleName);
    return responseOkObject(result);
  }

  @PUT
  @Path("roles/name/{roleName}")
  public Response updateByName(@Context UriInfo uriInfo, @PathParam("roleName") String roleName, ManageableRole role) {
    ManageableRole current = getRoleMaster().getByName(roleName);
    if (current.getObjectId().equals(role.getObjectId()) == false) {
      throw new IllegalArgumentException("Role does not match URI");
    }
    UniqueId result = getRoleMaster().update(role);
    return responseOkObject(result);
  }

  @DELETE
  @Path("roles/name/{roleName}")
  public void removeByName(@PathParam("roleName") String roleName) {
    getRoleMaster().removeByName(roleName);
  }

  @GET
  @Path("roles/name/{roleName}/eventHistory")
  public Response eventHistoryByName(@PathParam("roleName") String roleName) {
    RoleEventHistoryRequest request = new RoleEventHistoryRequest(roleName);
    RoleEventHistoryResult result = getRoleMaster().eventHistory(request);
    return responseOkObject(result);
  }

  @POST
  @Path("roles/account")
  public Response accountByName(UserAccount account) {
    UserAccount resolved = getRoleMaster().resolveAccount(account);
    return responseOkObject(resolved);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("roleSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("roles");
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
  public static URI uriRoleById(URI baseUri, ObjectIdentifiable objectId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/{objectId}");
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param roleName  the role name, not null
   * @return the URI, not null
   */
  public static URI uriRoleByName(URI baseUri, String roleName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/name/{roleName}");
    return bld.build(roleName);
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param roleName  the role name, not null
   * @return the URI, not null
   */
  public static URI uriNameExists(URI baseUri, String roleName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/exists/{roleName}");
    return bld.build(roleName);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, not null
   * @return the URI, not null
   */
  public static URI uriEventHistory(URI baseUri, RoleEventHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri);
    if (request.getObjectId() != null) {
      return bld.path("/roles/{objectId}/eventHistory").build(request.getObjectId());
    } else {
      return bld.path("/roles/name/{roleName}/eventHistory").build(request.getRoleName());
    }
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param account  the account, not null
   * @return the URI, not null
   */
  public static URI uriResolveRole(URI baseUri, UserAccount account) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/roles/account");
    return bld.build();
  }

}
