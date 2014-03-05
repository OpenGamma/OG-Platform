/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

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
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for regions.
 * <p>
 * The regions resource receives and processes RESTful calls to the region master.
 */
@Path("regionMaster")
public class DataRegionMasterResource extends AbstractDataResource {

  /**
   * The region master.
   */
  private final RegionMaster _regMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param regionMaster  the underlying region master, not null
   */
  public DataRegionMasterResource(final RegionMaster regionMaster) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regMaster = regionMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the region master.
   *
   * @return the region master, not null
   */
  public RegionMaster getRegionMaster() {
    return _regMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("regions")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("regionSearches")
  public Response search(RegionSearchRequest request) {
    RegionSearchResult result = getRegionMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("regions")
  public Response add(@Context UriInfo uriInfo, RegionDocument request) {
    RegionDocument result = getRegionMaster().add(request);
    URI createdUri = (new DataRegionResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("regions/{regionId}")
  public DataRegionResource findRegion(@PathParam("regionId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataRegionResource(this, id);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("regionSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("regions");
    return bld.build();
  }

}
