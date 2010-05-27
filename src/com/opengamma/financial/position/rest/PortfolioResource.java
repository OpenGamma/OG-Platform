/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for /portfolios.
 * <p>
 * The portfolios resource represents the whole of a position master.
 */
public class PortfolioResource {

  /**
   * The portfolios resource.
   */
  private final PortfoliosResource _portfoliosResource;
  /**
   * The portfolio unique identifier.
   */
  private final UniqueIdentifier _portfolioUid;

  /**
   * Creates the resource.
   * @param portfolioUid  the portfolio unique identifier, not null
   * @param portfoliosResource  the parent resource, not null
   */
  public PortfolioResource(final PortfoliosResource portfoliosResource, final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfoliosResource, "position master");
    ArgumentChecker.notNull(portfolioUid, "portfolio");
    _portfoliosResource = portfoliosResource;
    _portfolioUid = portfolioUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public PortfoliosResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getPortfoliosResource().getPositionMaster();
  }

  /**
   * Gets the portfolio unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPortfolioUid() {
    return _portfolioUid;
  }

  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getPortfoliosResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getAsHtml() {
    Portfolio portfolio = getPositionMaster().getPortfolio(_portfolioUid);
    if (portfolio == null) {
      return null;
    }
    return portfolio.toString() +
      "\n " + getUriInfo().getPath() +
      "\n " + getUriInfo().getAbsolutePath() +
      "\n " + getUriInfo().getBaseUri() +
      "\n " + getUriInfo().getMatchedResources() +
      "\n " + getUriInfo().getMatchedURIs() +
      "\n " + getUriInfo().getPathParameters() +
      "\n " + getUriInfo().getPathSegments() +
      "\n " + getUriInfo().getPathSegments().get(0).getPath() +
      "\n " + getUriInfo().getPathSegments().get(1).getPath() +
      "\n " + getUriInfo().getQueryParameters() +
      "\n " + getUriInfo().getRequestUri() +
      "\n " + getUriInfo().getRequestUri().toString() +
      "\n " + UriBuilder.fromResource(PortfoliosResource.class).build();
  }

}
