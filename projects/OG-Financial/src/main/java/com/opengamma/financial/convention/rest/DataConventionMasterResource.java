/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

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

import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionDocument;
import com.opengamma.financial.convention.ConventionMaster;
import com.opengamma.financial.convention.ConventionSearchRequest;
import com.opengamma.financial.convention.ConventionSearchResult;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.impl.DataConfigResource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for conventions.
 * <p>
 * The convention resource receives and processes RESTful calls to the master.
 */
@Path("conventionMaster")
public class DataConventionMasterResource extends AbstractDataResource {

  /**
   * The config master.
   */
  private final ConventionMaster _master;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param conventionMaster  the underlying config master, not null
   */
  public DataConventionMasterResource(final ConventionMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    _master = conventionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the convention master.
   *
   * @return the convention master, not null
   */
  public ConventionMaster getConventionMaster() {
    return _master;
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

  //@GET
  //@Path("metaData")
  //public Response metaData(@Context UriInfo uriInfo) {
  //  ConfigMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, ConfigMetaDataRequest.class);
  //  ConfigMetaDataResult result = getConfigMaster().metaData(request);
  //  return responseOkFudge(result);
  //}

  //@SuppressWarnings({"rawtypes", "unchecked" }) // necessary to stop Jersey issuing warnings due to <?>
  @POST
  @Path("conventionSearches")
  public Response search(ConventionSearchRequest request) {
    ConventionSearchResult result = getConventionMaster().searchConvention(request);
    return responseOkFudge(result);
  }

  @POST
  @Path("conventions")
  public Response add(@Context UriInfo uriInfo, ConventionDocument convention) {
    ConventionDocument result = getConventionMaster().add(convention);
    URI createdUri = DataConfigResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("conventions/{conventionId}")
  public DataConventionResource findConfig(@PathParam("conventionId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataConventionResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for convention meta-data.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, ConfigMetaDataRequest request) {
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
