/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;
import java.util.Stack;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.id.UniqueIdentifier;
import com.sleepycat.je.DatabaseNotFoundException;

/**
 * RESTful resource for all nodes in a portfolio.
 */
@Path("/portfolios/{portfolioId}/nodes")
public class WebPortfolioNodesResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodesResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @Path("{nodeId}")
  public WebPortfolioNodeResource findNode(@PathParam("nodeId") String idStr) {
    data().setUriNodeId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
    PortfolioTreeDocument portfolioDoc = data().getPortfolio();
    Stack<ManageablePortfolioNode> nodes = portfolioDoc.getPortfolio().getRootNode().getNodeStack(oid);
    if (nodes.isEmpty()) {
      throw new DatabaseNotFoundException("PortfoloNode not found: " + idStr);
    }
    data().setNode(nodes.pop());
    if (nodes.size() > 0) {
      data().setParentNode(nodes.pop());
    }
    return new WebPortfolioNodeResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    String portfolioId = data.getBestPortfolioUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodesResource.class).build(portfolioId);
  }

}
