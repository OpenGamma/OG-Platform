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

import com.opengamma.provider.security.SecurityEnhancer;
import com.opengamma.provider.security.SecurityEnhancerRequest;
import com.opengamma.provider.security.SecurityEnhancerResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the security enhancer.
 * <p>
 * This resource receives and processes RESTful calls to the security enhancer.
 */
@Path("securityEnhancer")
public class DataSecurityEnhancerResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final SecurityEnhancer _securityEnhancer;

  /**
   * Creates the resource, exposing the underlying enhancer over REST.
   * 
   * @param securityEnhancer  the underlying enhancer, not null
   */
  public DataSecurityEnhancerResource(final SecurityEnhancer securityEnhancer) {
    ArgumentChecker.notNull(securityEnhancer, "securityEnhancer");
    _securityEnhancer = securityEnhancer;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security enhancer.
   * 
   * @return the underlying security enhancer, not null
   */
  public SecurityEnhancer getSecurityEnhancer() {
    return _securityEnhancer;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("securityEnhance")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("securityEnhance")
  public Response getSecurity(SecurityEnhancerRequest request) {
    SecurityEnhancerResult result = getSecurityEnhancer().enhanceSecurities(request);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("securityEnhance");
    return bld.build();
  }

}
