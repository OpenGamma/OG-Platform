/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for all nodes in a portfolio.
 */
@Path("/portfolios/{portfolioUid}/nodes")
public class WebPortfolioNodesResource {

  /**
   * The portfolio resource.
   */
  private final WebPortfolioResource _portfolioResource;

  /**
   * Creates the resource.
   * @param portfolioResource  the parent resource, not null
   */
  public WebPortfolioNodesResource(final WebPortfolioResource portfolioResource) {
    ArgumentChecker.notNull(portfolioResource, "PortfolioResource");
    _portfolioResource = portfolioResource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio resource.
   * @return the portfolio resource, not null
   */
  public WebPortfolioResource getPortfolioResource() {
    return _portfolioResource;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
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
  @Path("{nodeUid}")
  public WebPortfolioNodeResource findNode(@PathParam("nodeUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new WebPortfolioNodeResource(this, uid);
  }

}
