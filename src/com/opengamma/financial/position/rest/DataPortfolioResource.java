/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a portfolio.
 */
@Path("/data/portfolios/{portfolioId}")
public class DataPortfolioResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfoliosResource _portfoliosResource;
  /**
   * The portfolio unique identifier.
   */
  private final UniqueIdentifier _urlPortfolioId;

  /**
   * Creates the resource.
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioId  the portfolio unique identifier, not null
   */
  public DataPortfolioResource(final DataPortfoliosResource portfoliosResource, final UniqueIdentifier portfolioId) {
    ArgumentChecker.notNull(portfoliosResource, "position master");
    ArgumentChecker.notNull(portfolioId, "portfolio");
    _portfoliosResource = portfoliosResource;
    _urlPortfolioId = portfolioId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public DataPortfoliosResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the portfolio identifier from the URL.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUrlPortfolioId() {
    return _urlPortfolioId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getPortfoliosResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPortfoliosResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(FudgeRest.MEDIA)
  public PortfolioTreeDocument get() {
    return getPositionMaster().getPortfolioTree(getUrlPortfolioId());
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  @Produces(FudgeRest.MEDIA)
  public Response put(PortfolioTreeDocument request) {
    if (getUrlPortfolioId().equals(request.getPortfolioId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URL");
    }
    PortfolioTreeDocument result = getPositionMaster().updatePortfolioTree(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  @Produces(FudgeRest.MEDIA)
  public Response delete() {
    getPositionMaster().removePortfolioTree(getUrlPortfolioId());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioId  the portfolio unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioId) {
    return uriInfo.getBaseUriBuilder().path(DataPortfolioResource.class).build(portfolioId);
  }

}
