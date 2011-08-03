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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * RESTful resource for a version of a portfolio.
 */
@Path("/portfolios/{portfolioId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioVersionResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioVersionResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
//  @GET
//  public String getHTML() {
//    FlexiBean out = createRootData();
//    return getFreemarker().build("portfolios/portfolioversion.ftl", out);
//  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getVersioned().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createPortfolioData();
    String json = getFreemarker().build("portfolios/jsonportfolio.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  private FlexiBean createPortfolioData() {
    PortfolioDocument doc = data().getPortfolio();
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setPositionIds(doc.getPortfolio().getRootNode().getPositionIds());
    PositionSearchResult positionsResult = data().getPositionMaster().search(positionSearch);
    
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
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioDocument latestPortfolioDoc = data().getPortfolio();
    PortfolioDocument versionedPortfolio = (PortfolioDocument) data().getVersioned();
    out.put("latestPortfolioDoc", latestPortfolioDoc);
    out.put("latestPortfolio", latestPortfolioDoc.getPortfolio());
    out.put("portfolioDoc", versionedPortfolio);
    out.put("portfolio", versionedPortfolio.getPortfolio());
    out.put("rootNode", versionedPortfolio.getPortfolio().getRootNode());
    out.put("childNodes", versionedPortfolio.getPortfolio().getRootNode().getChildNodes());
    out.put("deleted", !latestPortfolioDoc.isLatest());
    return out;
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
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueId overrideVersionId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioVersionResource.class).build(portfolioId, versionId);
  }

}
