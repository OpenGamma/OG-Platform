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

import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a portfolio tree.
 */
@Path("/data/portfoliotrees/{portfolioId}")
public class DataPortfolioTreeResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfolioTreesResource _portfoliosResource;
  /**
   * The portfolio unique identifier.
   */
  private final UniqueIdentifier _urlPortfolioId;

  /**
   * Creates the resource.
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioId  the portfolio unique identifier, not null
   */
  public DataPortfolioTreeResource(final DataPortfolioTreesResource portfoliosResource, final UniqueIdentifier portfolioId) {
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
  public DataPortfolioTreesResource getPortfoliosResource() {
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

  //-------------------------------------------------------------------------
  @GET
  public Response get() {
    PortfolioTreeDocument result = getPositionMaster().getPortfolioTree(getUrlPortfolioId());
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(PortfolioTreeDocument request) {
    if (getUrlPortfolioId().equals(request.getPortfolioId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URL");
    }
    PortfolioTreeDocument result = getPositionMaster().updatePortfolioTree(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getPositionMaster().removePortfolioTree(getUrlPortfolioId());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response searchHistoric(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioTreeSearchHistoricRequest request = decodeBean(PortfolioTreeSearchHistoricRequest.class, providers, msgBase64);
    if (getUrlPortfolioId().equals(request.getPortfolioId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URL");
    }
    PortfolioTreeSearchHistoricResult result = getPositionMaster().searchPortfolioTreeHistoric(request);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioId  the portfolio unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioId) {
    return uriInfo.getBaseUriBuilder().path("/portfoliotrees/{portfolioId}").build(portfolioId);
  }

}
