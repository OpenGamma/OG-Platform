/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * RESTful resource for all positions.
 * <p>
 * The positions resource represents the whole of a position master.
 * This is a logical URL as positions have unique identifiers.
 */
@Path("/data/positions")
@Consumes(FudgeRest.MEDIA)
@Produces(FudgeRest.MEDIA)
public class DataPositionsResource {

  /**
   * The injected position master.
   */
  private final PositionMaster _posMaster;
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;

  /**
   * Creates the resource.
   * @param posMaster  the position master, not null
   */
  public DataPositionsResource(final PositionMaster posMaster) {
    ArgumentChecker.notNull(posMaster, "PositionMaster");
    _posMaster = posMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _posMaster;
  }

  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  //-------------------------------------------------------------------------
  @GET
  public PositionSearchResult get(PositionSearchRequest request) {
    if (request == null) {
      request = new PositionSearchRequest();
      request.setPagingRequest(PagingRequest.FIRST_PAGE);
    }
    return getPositionMaster().searchPositions(request);
  }

  @POST
  public PositionDocument post(PositionDocument request) {
    return getPositionMaster().addPosition(request);
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public DataPositionResource findPosition(@PathParam("positionId") String idStr) {
    UniqueIdentifier id = UniqueIdentifier.parse(idStr);
    return new DataPositionResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all positions.
   * @param uriInfo  the URI information, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(DataPositionsResource.class).build();
  }

}
