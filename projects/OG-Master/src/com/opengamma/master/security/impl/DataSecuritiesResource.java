/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for securities.
 * <p>
 * The securities resource receives and processes RESTful calls to the security master.
 */
@Path("/secMaster")
public class DataSecuritiesResource extends AbstractDataResource {

  /**
   * The security master.
   */
  private final SecurityMaster _secMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param securityMaster  the underlying security master, not null
   */
  public DataSecuritiesResource(final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _secMaster = securityMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security master.
   * 
   * @return the security master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _secMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  public Response metaData(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    if (msgBase64 != null) {
      request = decodeBean(SecurityMetaDataRequest.class, providers, msgBase64);
    }
    SecurityMetaDataResult result = getSecurityMaster().metaData(request);
    return Response.ok(result).build();
  }

  @HEAD
  @Path("securities")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("securities")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    SecuritySearchRequest request = decodeBean(SecuritySearchRequest.class, providers, msgBase64);
    SecuritySearchResult result = getSecurityMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("securities")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, SecurityDocument request) {
    SecurityDocument result = getSecurityMaster().add(request);
    URI createdUri = DataSecurityResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("securities/{securityId}")
  public DataSecurityResource findSecurity(@PathParam("securityId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataSecurityResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for security meta-data.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/metaData");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

  /**
   * Builds a URI for all securities.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/securities");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
