/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the security loader.
 * <p>
 * This resource receives and processes RESTful calls to the security loader.
 */
@Path("securityLoader")
public class DataSecurityLoaderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final SecurityLoader _securityLoader;

  /**
   * Creates the resource, exposing the underlying loader over REST.
   * 
   * @param securityLoader  the underlying loader, not null
   */
  public DataSecurityLoaderResource(final SecurityLoader securityLoader) {
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    _securityLoader = securityLoader;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security loader.
   * 
   * @return the underlying security loader, not null
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("securityLoad")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("securityLoad")
  public Response loadSecurities(SecurityLoaderRequest request) {
    SecurityLoaderResult result = getSecurityLoader().loadSecurities(request);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("securityLoad");
    return bld.build();
  }

}
