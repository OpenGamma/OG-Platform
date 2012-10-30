/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions/{positionId}")
public class WebPortfolioNodePositionResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodePositionResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    ObjectId positionId = ObjectId.parse(data().getUriPositionId());
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      ManageablePortfolioNode node = data().getNode();
      if (node.getPositionIds().remove(positionId) == false) {
        throw new DataNotFoundException("Position id not found: " + positionId);
      }
      doc = data().getPortfolioMaster().update(doc);
    }
    return Response.seeOther(WebPortfolioNodeResource.uri(data())).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    ObjectId positionId = ObjectId.parse(data().getUriPositionId());
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      ManageablePortfolioNode node = data().getNode();
      if (node.getPositionIds().remove(positionId) == false) {
        throw new DataNotFoundException("Position id not found: " + positionId);
      }
      doc = data().getPortfolioMaster().update(doc);
    }
    return Response.ok().build();
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
   * @param overridePositionId  the override node id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final ObjectIdentifiable overridePositionId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(null);
    String positionId = overridePositionId.getObjectId().toString();
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionResource.class).build(portfolioId, nodeId, positionId);
  }

}
