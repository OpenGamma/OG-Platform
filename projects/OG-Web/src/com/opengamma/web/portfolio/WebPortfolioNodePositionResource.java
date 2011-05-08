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

import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.sleepycat.je.DatabaseNotFoundException;

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
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    ObjectIdentifier positionId = ObjectIdentifier.parse(data().getUriPositionId());
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      ManageablePortfolioNode node = data().getNode();
      if (node.getPositionIds().remove(positionId) == false) {
        throw new DatabaseNotFoundException("Position id not found: " + positionId);
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
  public static URI uri(final WebPortfoliosData data, final UniqueIdentifier overridePositionId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(null);
    String positionId = data.getBestNodeUriId(overridePositionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionResource.class).build(portfolioId, nodeId, positionId);
  }

}
