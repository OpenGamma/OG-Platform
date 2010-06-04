/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionAccumulator;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for all positions in a portfolio.
 */
@Path("/portfolios/{portfolioUid}/positions")
public class PositionsResource {

  /**
   * The portfolio resource.
   */
  private final PortfolioResource _portfolioResource;

  /**
   * Creates the resource.
   * @param portfolioResource  the parent resource, not null
   */
  public PositionsResource(final PortfolioResource portfolioResource) {
    ArgumentChecker.notNull(portfolioResource, "PortfolioResource");
    _portfolioResource = portfolioResource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio resource.
   * @return the portfolio resource, not null
   */
  public PortfolioResource getPortfolioResource() {
    return _portfolioResource;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public ManagablePositionMaster getPositionMaster() {
    return getPortfolioResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPortfolioResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml() {
    UniqueIdentifier portfolioUid = getPortfolioResource().getPortfolioUid();
    Portfolio portfolio = getPositionMaster().getPortfolio(portfolioUid);
    if (portfolio == null) {
      return null;
    }
    Set<Position> positions = PositionAccumulator.getAccumulatedPositions(portfolio.getRootNode());
    String html = "<html>" +
      "<head><title>Positions</title></head>" +
      "<body>" +
      "<h2>Positions</h2>" +
      "<p><table>";
    for (Position position : positions) {
      URI uri = getUriInfo().getRequestUriBuilder().path(position.getUniqueIdentifier().toString()).build();
      html += "<tr><td><a href=\"" + uri + "\">" + position.getUniqueIdentifier() + "</a></td></tr>";
    }
    html += "</table></p></body></html>";
    return html;
  }

  //-------------------------------------------------------------------------
  @Path("{positionUid}")
  public PositionResource findPosition(@PathParam("positionUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new PositionResource(this, uid);
  }

}
