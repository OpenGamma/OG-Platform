/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the permission check provider.
 * <p>
 * This resource receives and processes RESTful calls to the permission check provider.
 */
@Path("permissionCheckProvider")
public class DataPermissionCheckProviderResource extends AbstractDataResource {

  /**
   * The permission check provider.
   */
  private final PermissionCheckProvider _permissionCheckProvider;

  /**
   * Creates the resource, exposing the underlying permission check provider over REST.
   * 
   * @param permissionCheckProvider the underlying permission check provider, not null
   */
  public DataPermissionCheckProviderResource(final PermissionCheckProvider permissionCheckProvider) {
    ArgumentChecker.notNull(permissionCheckProvider, "permissionCheckProvider");
    _permissionCheckProvider = permissionCheckProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the permissionCheckProvider.
   * 
   * @return the permissionCheckProvider, not null
   */
  public PermissionCheckProvider getPermissionCheckProvider() {
    return _permissionCheckProvider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }


  @HEAD
  @Path("permissionCheckGet")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("permissionCheckGet")
  public Response getPermissionCheck(PermissionCheckProviderRequest request) {
    PermissionCheckProviderResult result = getPermissionCheckProvider().isPermitted(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("permissionCheckGet");
    return bld.build();
  }

}
