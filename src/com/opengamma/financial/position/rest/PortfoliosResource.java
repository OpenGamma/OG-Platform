/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for /portfolios.
 * <p>
 * The portfolios resource represents the whole of a position master.
 */
@Path("/portfolios")
public class PortfoliosResource {

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
  public PortfoliosResource(final PositionMaster posMaster) {
    ArgumentChecker.notNull(posMaster, "position master");
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
  @Produces(MediaType.TEXT_PLAIN)
  public String getAsHtml() {
    Set<UniqueIdentifier> uids = getPositionMaster().getPortfolioIds();
    return uids.toString() +
      "\n " + getUriInfo().getPath() +
      "\n " + getUriInfo().getAbsolutePath() +
      "\n " + getUriInfo().getBaseUri() +
      "\n " + getUriInfo().getMatchedResources() +
      "\n " + getUriInfo().getMatchedURIs() +
      "\n " + getUriInfo().getPathParameters() +
      "\n " + getUriInfo().getPathSegments() +
      "\n " + getUriInfo().getQueryParameters() +
      "\n " + getUriInfo().getRequestUri();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioUid}")
  public PortfolioResource findPortfolio(@PathParam("portfolioUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new PortfolioResource(this, uid);
  }

}
