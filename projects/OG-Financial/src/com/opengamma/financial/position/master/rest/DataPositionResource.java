/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a position.
 */
@Path("/data/positions/{positionId}")
public class DataPositionResource extends AbstractDataResource {

  /**
   * The positions resource.
   */
  private final DataPositionsResource _positionsResource;
  /**
   * The identifier specified in the URI.
   */
  private UniqueIdentifier _urlResourceId;

  /**
   * Creates the resource.
   * @param positionsResource  the parent resource, not null
   * @param positionId  the position unique identifier, not null
   */
  public DataPositionResource(final DataPositionsResource positionsResource, final UniqueIdentifier positionId) {
    ArgumentChecker.notNull(positionsResource, "position master");
    ArgumentChecker.notNull(positionId, "position");
    _positionsResource = positionsResource;
    _urlResourceId = positionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the positions resource.
   * @return the positions resource, not null
   */
  public DataPositionsResource getPositionsResource() {
    return _positionsResource;
  }

  /**
   * Gets the position identifier from the URL.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUrlPositionId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getPositionsResource().getPositionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get() {
    PositionDocument result = getPositionMaster().getPosition(getUrlPositionId());
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(PositionDocument request) {
    if (getUrlPositionId().equalsIgnoringVersion(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    PositionDocument result = getPositionMaster().updatePosition(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getPositionMaster().removePosition(getUrlPositionId());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PositionHistoryRequest request = decodeBean(PositionHistoryRequest.class, providers, msgBase64);
    if (getUrlPositionId().equalsIgnoringVersion(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PositionHistoryResult result = getPositionMaster().historyPosition(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    _urlResourceId = _urlResourceId.withVersion(versionId);
    return get();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * @param baseUri  the base URI, not null
   * @param id  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, UniqueIdentifier id) {
    return UriBuilder.fromUri(baseUri).path("/positions/{positionId}").build(id.toLatest());
  }

  /**
   * Builds a URI for the versions of the resource.
   * @param baseUri  the base URI, not null
   * @param id  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, UniqueIdentifier id, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/positions/{positionId}/versions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build(id.toLatest());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * @param baseUri  the base URI, not null
   * @param uid  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueIdentifier uid) {
    return UriBuilder.fromUri(baseUri).path("/positions/{positionId}/versions/{versionId}")
      .build(uid.toLatest(), uid.getVersion());
  }

}
