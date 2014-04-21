/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for an region.
 */
public class DataRegionResource
    extends AbstractDocumentDataResource<RegionDocument> {

  /**
   * The regions resource.
   */
  private final DataRegionMasterResource _regionsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataRegionResource() {
    _regionsResource = null;
  }
  
  /**
   * Creates the resource.
   *
   * @param regionsResource  the parent resource, not null
   * @param regionId  the region unique identifier, not null
   */
  public DataRegionResource(final DataRegionMasterResource regionsResource, final ObjectId regionId) {
    ArgumentChecker.notNull(regionsResource, "regionsResource");
    ArgumentChecker.notNull(regionId, "region");
    _regionsResource = regionsResource;
    _urlResourceId = regionId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the regions resource.
   *
   * @return the regions resource, not null
   */
  public DataRegionMasterResource getRegionsResource() {
    return _regionsResource;
  }

  /**
   * Gets the region identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the region master.
   *
   * @return the region master, not null
   */
  @Override
  protected RegionMaster getMaster() {
    return getRegionsResource().getRegionMaster();
  }

  
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    RegionHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, RegionHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    RegionHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, RegionDocument request) {
    return super.update(uriInfo, request);
  }

  @DELETE
  public void remove() {
    super.remove();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    return super.getVersioned(versionId);
  }


  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") String versionId, List<RegionDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<RegionDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<RegionDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "regions";
  }

}
