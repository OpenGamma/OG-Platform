/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.net.URI;

import javax.time.Instant;
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

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for a position.
 */
@Path("/posMaster/positions/{positionId}")
public class DataPositionResource extends AbstractDataResource {

  /**
   * The positions resource.
   */
  private final DataPositionsResource _positionsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param positionsResource  the parent resource, not null
   * @param positionId  the position unique identifier, not null
   */
  public DataPositionResource(final DataPositionsResource positionsResource, final ObjectId positionId) {
    ArgumentChecker.notNull(positionsResource, "positionsResource");
    ArgumentChecker.notNull(positionId, "position");
    _positionsResource = positionsResource;
    _urlResourceId = positionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the positions resource.
   * 
   * @return the positions resource, not null
   */
  public DataPositionsResource getPositionsResource() {
    return _positionsResource;
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
    return getPositionsResource().getPositionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    PositionDocument result = getPositionMaster().get(getUrlPositionId(), VersionCorrection.of(v, c));
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(PositionDocument request) {
    if (getUrlPositionId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PositionDocument result = getPositionMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getPositionMaster().remove(getUrlPositionId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PositionHistoryRequest request = decodeBean(PositionHistoryRequest.class, providers, msgBase64);
    if (getUrlPositionId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PositionHistoryResult result = getPositionMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    PositionDocument result = getPositionMaster().get(getUrlPositionId().atVersion(versionId));
    return Response.ok(result).build();
  }

  @GET
  @Path("trades/{tradeId}")
  public Response getTrade(@PathParam("tradeId") String idStr) {
    UniqueId tradeId = UniqueId.parse(idStr);
    ManageableTrade result = getPositionMaster().getTrade(tradeId);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param versionCorrection  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/posMaster/positions/{positionId}");
    if (versionCorrection != null && versionCorrection.getVersionAsOf() != null) {
      b.queryParam("versionAsOf", versionCorrection.getVersionAsOf());
    }
    if (versionCorrection != null && versionCorrection.getCorrectedTo() != null) {
      b.queryParam("correctedTo", versionCorrection.getCorrectedTo());
    }
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/posMaster/positions/{positionId}/versions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/posMaster/positions/{positionId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

  /**
   * Builds a URI for a specific node.
   * 
   * @param baseUri  the base URI, not null
   * @param tradeId  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriTrade(URI baseUri, UniqueId tradeId) {
    return UriBuilder.fromUri(baseUri).path("/posMaster/positions/{positionId}/trades/{tradeId}")
      .build("-", tradeId);  // TODO: probably could do with a better URI
  }

}
