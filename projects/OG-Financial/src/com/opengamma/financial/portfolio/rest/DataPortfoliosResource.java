/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for all portfolios.
 * <p>
 * The portfolios resource represents the whole of a portfolio master.
 */
@Path("/data/portfolios")
public class DataPortfoliosResource extends AbstractDataResource {

  /**
   * The injected portfolio master.
   */
  private final PortfolioMaster _prtMaster;

  /**
   * Creates the resource.
   * 
   * @param portfolioMaster  the portfolio master, not null
   */
  public DataPortfoliosResource(final PortfolioMaster portfolioMaster) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    _prtMaster = portfolioMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * 
   * @return the portfolio master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _prtMaster;
  }

  //-------------------------------------------------------------------------
  @HEAD
  public Response status() {
    // The root GET handler tries to return the entire content of the portfolio master. Something faster
    // to respond is needed for head requests that are used to check whether the master is available. 
    return Response.ok().build();
  }

  @GET
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioSearchRequest request = decodeBean(PortfolioSearchRequest.class, providers, msgBase64);
    PortfolioSearchResult result = getPortfolioMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, PortfolioDocument request) {
    PortfolioDocument result = getPortfolioMaster().add(request);
    return Response.created(DataPortfolioResource.uri(uriInfo.getBaseUri(), result.getUniqueId(), null)).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("nodes/{nodeId}")
  public DataPortfolioNodeResource findPortfolioNode(@PathParam("nodeId") String idStr) {
    UniqueIdentifier id = UniqueIdentifier.parse(idStr);
    return new DataPortfolioNodeResource(this, id);
  }

  @Path("{portfolioId}")
  public DataPortfolioResource findPortfolio(@PathParam("portfolioId") String idStr) {
    UniqueIdentifier id = UniqueIdentifier.parse(idStr);
    return new DataPortfolioResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all portfolios.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfolios");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
