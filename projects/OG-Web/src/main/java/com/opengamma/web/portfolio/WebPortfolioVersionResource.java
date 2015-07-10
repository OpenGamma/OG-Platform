/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * RESTful resource for a version of a portfolio.
 */
@Path("/portfolios/{portfolioId}/versions/{versionId}")
public class WebPortfolioVersionResource extends WebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioVersionResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }
    
  //-------------------------------------------------------------------------
  @Override
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createPortfolioData();
    return getFreemarker().build(HTML_DIR + "portfolio.ftl", out);
  }

  @Override
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON() {
    FlexiBean out = createPortfolioData();
    return Response.ok(getFreemarker().build(JSON_DIR + "portfolio.ftl", out)).build();
  }

  private FlexiBean createPortfolioData() {
    PortfolioDocument doc = data().getVersioned();
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setPositionObjectIds(doc.getPortfolio().getRootNode().getPositionIds());
    PositionSearchResult positionsResult = data().getPositionMaster().search(positionSearch);
    resolveSecurities(positionsResult.getPositions());

    FlexiBean out = createRootData();
    out.put("positionsResult", positionsResult);
    out.put("positions", positionsResult.getPositions());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioDocument doc = data().getVersioned();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("childNodes", doc.getPortfolio().getRootNode().getChildNodes());
    out.put("deleted", !doc.isLatest());
    out.put("rootNode", doc.getPortfolio().getRootNode());
    return out;
  }

  //-------------------------------------------------------------------------
  @Override
  @Path("nodes")
  public WebPortfolioNodesResource findNodes() {
    return new WebPortfolioVersionNodesResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overridePortfolioId  the override portfolio id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueId overridePortfolioId) {
    String portfolioId = data.getBestPortfolioUriId(overridePortfolioId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioResource.class).build(portfolioId);
  }

}
