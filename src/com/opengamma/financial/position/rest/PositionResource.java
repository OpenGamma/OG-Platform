/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.PositionSummary;
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

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPortfolioUid() {
    return getPositionsResource().getPortfolioUid();
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
    PositionSummary summary = getPositionMaster().getPositionSummary(_positionUid);
    if (summary == null) {
      return null;
    }
    String html = "<html>" +
      "<head><title>Position - " + summary.getUniqueIdentifier().toLatest() + "</title></head>" +
      "<body>" +
      "<h2>Position - " + summary.getUniqueIdentifier().toLatest() + "</h2>" +
      "<p>" +
      "Version: " + summary.getUniqueIdentifier().getVersion() + "<br />" +
      "Quantity: " + summary.getQuantity() + "<br />" +
      "Security: " + summary.getSecurityKey() + "</p>" +
      "<h2>Links</h2>" +
      "<p>" +
      "<a href=\"" + PortfolioNodeResource.uri(getUriInfo(), summary.getPortfolioUid(), summary.getParentNodeUid().toLatest()) + "\">Parent node</a><br />" +
      "<a href=\"" + PortfolioResource.uri(getUriInfo(), summary.getPortfolioUid().toLatest()) + "\">Portfolio</a><br />" +
      "<a href=\"" + PortfoliosResource.uri(getUriInfo()) + "\">Portfolio search</a><br />" +
      "</body>" +
      "</html>";
    return html;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   * @param positionUid  the position unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioUid, UniqueIdentifier positionUid) {
    return uriInfo.getBaseUriBuilder().path(PositionResource.class).build(portfolioUid.toLatest(), positionUid);
  }

}
