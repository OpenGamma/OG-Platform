/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions")
public class WebPortfolioNodePositionsResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodePositionsResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @POST
  @Produces(MediaType.TEXT_HTML)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("positionurl") String positionUrlStr) {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(new WebPortfolioNodeResource(this).getHTML()).build();
    }
    
    positionUrlStr = StringUtils.trimToNull(positionUrlStr);
    if (positionUrlStr == null) {
      FlexiBean out = createRootData();
      out.put("err_positionUrlMissing", true);
      String html = getFreemarker().build("portfolios/portfolionodepositions-add.ftl", out);
      return Response.ok(html).build();
    }
    UniqueIdentifier posUid = null;
    try {
      new URI(positionUrlStr);  // validates whole URI
      String uidStr = StringUtils.substringAfterLast(positionUrlStr, "/positions/");
      uidStr = StringUtils.substringBefore(uidStr, "/");
      posUid = UniqueIdentifier.parse(uidStr);
    } catch (Exception ex) {
      FlexiBean out = createRootData();
      out.put("err_positionUrlInvalid", true);
      String html = getFreemarker().build("portfolios/portfolionodepositions-add.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = addPosition(doc, posUid);
    return Response.seeOther(uri).build();
  }

  private URI addPosition(PortfolioDocument doc, UniqueIdentifier posUid) {
    ManageablePortfolioNode node = data().getNode();
    URI uri = WebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    if (node.getPositionIds().contains(posUid) == false) {
      node.addPosition(posUid);
      doc = data().getPortfolioMaster().update(doc);
      data().setPortfolio(doc);
    }
    return uri;
  }
 
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(@FormParam("uid") String uidStr) {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(new WebPortfolioNodeResource(this).getHTML()).build();
    }
    uidStr = StringUtils.trimToNull(uidStr);
    if (uidStr == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    UniqueIdentifier posUid = null;
    try {
      posUid = UniqueIdentifier.parse(uidStr);
    } catch (Exception ex) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    URI uri = addPosition(doc, posUid);
    return Response.created(uri).build();
  }
  
  

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioDocument doc = data().getPortfolio();
    ManageablePortfolioNode node = data().getNode();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("parentNode", data().getParentNode());
    out.put("node", node);
    out.put("childNodes", node.getChildNodes());
    out.put("deleted", !doc.isLatest());
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
   * @param overrideNodeId  the override node id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueIdentifier overrideNodeId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(overrideNodeId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionsResource.class).build(portfolioId, nodeId);
  }

}
