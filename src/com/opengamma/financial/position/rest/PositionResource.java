/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a postion in a portfolio.
 */
@Path("/portfolios/{portfolioUid}/positions/{positionUid}")
public class PositionResource {

  /**
   * The positions resource.
   */
  private final PositionsResource _positionsResource;
  /**
   * The position unique identifier.
   */
  private final UniqueIdentifier _positionUid;

  /**
   * Creates the resource.
   * @param positionsResource  the parent resource, not null
   * @param positionUid  the position unique identifier, not null
   */
  public PositionResource(final PositionsResource positionsResource, final UniqueIdentifier positionUid) {
    ArgumentChecker.notNull(positionsResource, "PositionsResource");
    ArgumentChecker.notNull(positionUid, "position");
    _positionsResource = positionsResource;
    _positionUid = positionUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the positions resource.
   * @return the positions resource, not null
   */
  public PositionsResource getPositionsResource() {
    return _positionsResource;
  }

  /**
   * Gets the position unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPositionUid() {
    return _positionUid;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public ManagablePositionMaster getPositionMaster() {
    return getPositionsResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPositionsResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml() {
    Position position = getPositionMaster().getPosition(_positionUid);
    if (position == null) {
      return null;
    }
    String html = "<html>" +
      "<head><title>Position - " + position.getUniqueIdentifier() + "</title></head>" +
      "<body>" +
      "<h2>Position - " + position.getUniqueIdentifier() + "</h2>" +
      "<p>" + position.getQuantity() + " " + position.getSecurityKey() + "</p>" +
      "</body>" +
      "</html>";
    return html;
  }

}
