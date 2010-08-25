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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.id.UniqueIdentifier;
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
   * The position unique identifier.
   */
  private final UniqueIdentifier _urlPositionId;

  /**
   * Creates the resource.
   * @param positionsResource  the parent resource, not null
   * @param positionId  the position unique identifier, not null
   */
  public DataPositionResource(final DataPositionsResource positionsResource, final UniqueIdentifier positionId) {
    ArgumentChecker.notNull(positionsResource, "position master");
    ArgumentChecker.notNull(positionId, "position");
    _positionsResource = positionsResource;
    _urlPositionId = positionId;
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
    return _urlPositionId;
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
    if (getUrlPositionId().equalsIgnoringVersion(request.getPositionId()) == false) {
      throw new IllegalArgumentException("Document positionId does not match URI");
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
  public Response searchHistoric(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PositionSearchHistoricRequest request = decodeBean(PositionSearchHistoricRequest.class, providers, msgBase64);
    if (getUrlPositionId().equalsIgnoringVersion(request.getPositionId()) == false) {
      throw new IllegalArgumentException("Document positionId does not match URI");
    }
    PositionSearchHistoricResult result = getPositionMaster().searchPositionHistoric(request);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a position.
   * @param uriInfo  the URI information, not null
   * @param positionId  the position unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier positionId) {
    return uriInfo.getBaseUriBuilder().path("/positions/{positionId}").build(positionId);
  }

}
