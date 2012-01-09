/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

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

import com.opengamma.core.change.ChangeManagerResource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for portfolios.
 * <p>
 * The portfolios resource receives and processes RESTful calls to the portfolio master.
 */
@Path("/data/prtMaster")
public class DataPortfoliosResource extends AbstractDataResource {

  /**
   * The portfolio master.
   */
  private final PortfolioMaster _prtMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param portfolioMaster  the underlying portfolio master, not null
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
  @Path("portfolios")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("portfolios")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioSearchRequest request = decodeBean(PortfolioSearchRequest.class, providers, msgBase64);
    PortfolioSearchResult result = getPortfolioMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("portfolios")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, PortfolioDocument request) {
    PortfolioDocument result = getPortfolioMaster().add(request);
    URI createdUri = DataPortfolioResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("nodes/{nodeId}")
  public DataPortfolioNodeResource findPortfolioNode(@PathParam("nodeId") String idStr) {
    UniqueId id = UniqueId.parse(idStr);
    return new DataPortfolioNodeResource(this, id);
  }

  @Path("portfolios/{portfolioId}")
  public DataPortfolioResource findPortfolio(@PathParam("portfolioId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataPortfolioResource(this, id);
  }
  
  //-------------------------------------------------------------------------
  // REVIEW jonathan 2011-12-28 -- to be removed when the change topic name is exposed as part of the component config
  @Path("portfolios/changeManager")
  public ChangeManagerResource getChangeManager() {
    return new ChangeManagerResource(getPortfolioMaster().changeManager());
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
    // TODO remove this hack
    UriBuilder bld = UriBuilder.fromUri(baseUri);
    if (!baseUri.getPath().endsWith("prtMaster/")) {
      bld.path("/prtMaster");
    }
    bld.path("/portfolios");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
