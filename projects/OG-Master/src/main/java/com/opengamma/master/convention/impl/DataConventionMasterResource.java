/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

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
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for conventions.
 * <p>
 * The conventions resource receives and processes RESTful calls to the convention master.
 */
@Path("conventionMaster")
public class DataConventionMasterResource extends AbstractDataResource {

  /**
   * The convention master.
   */
  private final ConventionMaster _exgMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param conventionMaster  the underlying convention master, not null
   */
  public DataConventionMasterResource(final ConventionMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    _exgMaster = conventionMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the convention master.
   *
   * @return the convention master, not null
   */
  public ConventionMaster getConventionMaster() {
    return _exgMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("conventions")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @GET
  @Path("metaData")
  public Response metaData(@Context UriInfo uriInfo) {
    ConventionMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, ConventionMetaDataRequest.class);
    ConventionMetaDataResult result = getConventionMaster().metaData(request);
    return responseOkObject(result);
  }

  @POST
  @Path("conventionSearches")
  public Response search(ConventionSearchRequest request) {
    ConventionSearchResult result = getConventionMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("conventions")
  public Response add(@Context UriInfo uriInfo, ConventionDocument request) {
    ConventionDocument result = getConventionMaster().add(request);
    URI createdUri = (new DataConventionResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("conventions/{conventionId}")
  public DataConventionResource findConvention(@PathParam("conventionId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataConventionResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for security meta-data.
   * 
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, ConventionMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions");
    return bld.build();
  }

}
