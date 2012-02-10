/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a position in the position master.
 */
public class DataPositionResource extends AbstractDataResource {

  /**
   * The parent resource.
   */
  private final DataPositionMasterResource _parentResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param parentResource  the parent resource, not null
   * @param positionId  the position unique identifier, not null
   */
  public DataPositionResource(final DataPositionMasterResource parentResource, final ObjectId positionId) {
    ArgumentChecker.notNull(parentResource, "parentResource");
    ArgumentChecker.notNull(positionId, "position");
    _parentResource = parentResource;
    _urlResourceId = positionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent resource.
   * 
   * @return the parent resource, not null
   */
  public DataPositionMasterResource getParentResource() {
    return _parentResource;
  }

  /**
   * Gets the position identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlPositionId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * 
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getParentResource().getPositionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    PositionDocument result = getPositionMaster().get(getUrlPositionId(), vc);
    return Response.ok(result).build();
  }

  @POST
  public Response update(@Context UriInfo uriInfo, PositionDocument request) {
    if (getUrlPositionId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PositionDocument result = getPositionMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  @DELETE
  public Response remove() {
    getPositionMaster().remove(getUrlPositionId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    PositionHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, PositionHistoryRequest.class);
    if (getUrlPositionId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PositionHistoryResult result = getPositionMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlPositionId().atVersion(versionId);
    PositionDocument result = getPositionMaster().get(uniqueId);
    return Response.ok(result).build();
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, PositionDocument request) {
    UniqueId uniqueId = getUrlPositionId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    PositionDocument result = getPositionMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/positions/{positionId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, PositionHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/positions/{positionId}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    return UriBuilder.fromUri(baseUri).path("/positions/{positionId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
