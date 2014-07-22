/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

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
  public String getHTML() {
    FlexiBean out = createPortfolioData();
    return getFreemarker().build(HTML_DIR + "portfolio.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON() {
    FlexiBean out = createPortfolioData();
    return Response.ok(getFreemarker().build(JSON_DIR + "portfolio.ftl", out)).build();
  }

  private FlexiBean createPortfolioData() {
    PortfolioDocument doc = data().getPortfolio();
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
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(@FormParam("name") String name, @FormParam("hidden") Boolean isHidden) {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    DocumentVisibility visibility = BooleanUtils.isTrue(isHidden) ? DocumentVisibility.HIDDEN : DocumentVisibility.VISIBLE;
    if (name == null) {
      FlexiBean out = createRootData();
      out.put("err_nameMissing", true);
      String html = getFreemarker().build(HTML_DIR + "portfolio-update.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = updatePortfolio(name, visibility, doc);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(@FormParam("name") String name, @FormParam("hidden") Boolean isHidden) {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    name = StringUtils.trimToNull(name);
    DocumentVisibility visibility = BooleanUtils.isTrue(isHidden) ? DocumentVisibility.HIDDEN : DocumentVisibility.VISIBLE;
    updatePortfolio(name, visibility, doc);
    return Response.ok().build();
  }

  private URI updatePortfolio(String name, DocumentVisibility visibility, PortfolioDocument doc) {
    doc.getPortfolio().setName(name);
    doc.setVisibility(visibility);
    doc = data().getPortfolioMaster().update(doc);
    data().setPortfolio(doc);
    URI uri = WebPortfolioResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }  
    data().getPortfolioMaster().remove(doc.getUniqueId());
    URI uri = WebPortfolioResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      data().getPortfolioMaster().remove(doc.getUniqueId());
    }  
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioDocument doc = data().getPortfolio();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("childNodes", doc.getPortfolio().getRootNode().getChildNodes());
    out.put("deleted", !doc.isLatest());
    out.put("rootNode", doc.getPortfolio().getRootNode());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("nodes")
  public WebPortfolioNodesResource findNodes() {
    return new WebPortfolioNodesResource(this);
  }

  @Path("versions")
  public WebPortfolioVersionsResource findVersions() {
    return new WebPortfolioVersionsResource(this);
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
