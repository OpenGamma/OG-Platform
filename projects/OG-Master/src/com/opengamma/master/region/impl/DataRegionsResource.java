/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

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
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for regions.
 * <p>
 * The regions resource receives and processes RESTful calls to the region master.
 */
@Path("/regMaster")
public class DataRegionsResource extends AbstractDataResource {

  /**
   * The region master.
   */
  private final RegionMaster _regMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param regionMaster  the underlying region master, not null
   */
  public DataRegionsResource(final RegionMaster regionMaster) {
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
  @HEAD
  @Path("regions")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("regions")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    RegionSearchRequest request = decodeBean(RegionSearchRequest.class, providers, msgBase64);
    RegionSearchResult result = getRegionMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("regions")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, RegionDocument request) {
    RegionDocument result = getRegionMaster().add(request);
    URI createdUri = DataRegionResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("regions/{regionId}")
  public DataRegionResource findRegion(@PathParam("regionId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataRegionResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all regions.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/regions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
