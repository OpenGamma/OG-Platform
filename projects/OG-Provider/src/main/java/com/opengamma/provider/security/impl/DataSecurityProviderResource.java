/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the security provider.
 * <p>
 * This resource receives and processes RESTful calls to the security provider.
 */
@Path("securityProvider")
public class DataSecurityProviderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final SecurityProvider _securityProvider;

  /**
   * Creates the resource, exposing the underlying provider over REST.
   * 
   * @param securityProvider  the underlying provider, not null
   */
  public DataSecurityProviderResource(final SecurityProvider securityProvider) {
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    _securityProvider = securityProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security provider.
   * 
   * @return the security provider, not null
   */
  public SecurityProvider getSecurityProvider() {
    return _securityProvider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("securityGet")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("securityGet")
  public Response getSecurity(SecurityProviderRequest request) {
    SecurityProviderResult result = getSecurityProvider().getSecurities(request);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("securityGet");
    return bld.build();
  }

}
