/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.web;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;

/**
 * RESTful resource for a portfolio.
 */
@Path("/portfolios/{portfolioId}")
public class WebPortfolioResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    PortfolioTreeDocument doc = data().getPortfolio();
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setParentNodeId(doc.getPortfolio().getRootNode().getUniqueIdentifier());
    PositionSearchResult positionsResult = data().getPositionMaster().searchPositions(positionSearch);
    
    FlexiBean data = getFreemarker().createRootData();
    data.put("portfolioDoc", doc);
    data.put("positionsResult", positionsResult);
    data.put("portfolio", doc.getPortfolio());
    data.put("childNodes", doc.getPortfolio().getRootNode().getChildNodes());
    data.put("positions", positionsResult.getPositions());
    data.put("uris", new WebPortfoliosUris(data()));
    return getFreemarker().build("portfolios/portfolio.ftl", data);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    PortfolioTreeDocument doc = data().getPortfolio();
    doc.getPortfolio().setName(StringUtils.trim(name));
    doc = data().getPositionMaster().updatePortfolioTree(doc);
    data().setPortfolio(doc);
    URI uri = WebPortfolioResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    PortfolioTreeDocument doc = data().getPortfolio();
    data().getPositionMaster().removePortfolioTree(doc.getPortfolioId());
    URI uri = WebPortfoliosResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("nodes")
  public WebPortfolioNodesResource findNodes() {
    return new WebPortfolioNodesResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    String portfolioId = data.getBestPortfolioUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioResource.class).build(portfolioId);
  }

}
