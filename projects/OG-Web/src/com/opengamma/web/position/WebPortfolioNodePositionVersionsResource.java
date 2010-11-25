/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions/{positionId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioNodePositionVersionsResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodePositionVersionsResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    PositionHistoryRequest request = new PositionHistoryRequest(data().getPosition().getUniqueId());
    PositionHistoryResult result = data().getPositionMaster().historyPosition(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getPositions());
    return getFreemarker().build("portfolios/portfolionodepositionversions.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioTreeDocument treeDoc = data().getPortfolio();
    PositionDocument positionDoc = data().getPosition();
    out.put("portfolioDoc", treeDoc);
    out.put("portfolio", treeDoc.getPortfolio());
    out.put("node", data().getNode());
    out.put("positionDoc", positionDoc);
    out.put("position", positionDoc.getPosition());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebPortfolioNodePositionVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    PositionDocument position = data().getPosition();
    UniqueIdentifier combined = position.getUniqueId().withVersion(idStr);
    if (position.getUniqueId().equals(combined) == false) {
      PositionDocument versioned = data().getPositionMaster().getPosition(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(position);
    }
    return new WebPortfolioNodePositionVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(null);
    String positionId = data.getBestPositionUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionVersionsResource.class).build(portfolioId, nodeId, positionId);
  }

}
