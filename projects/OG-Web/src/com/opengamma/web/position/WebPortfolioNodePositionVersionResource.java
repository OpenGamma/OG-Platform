/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PositionDocument;

/**
 * RESTful resource for a version of a position.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions/{positionId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioNodePositionVersionResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodePositionVersionResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("portfolios/portfolionodepositionversion.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioTreeDocument treeDoc = data().getPortfolio();
    PositionDocument latestPositionDoc = data().getPosition();
    PositionDocument versionedPosition = (PositionDocument) data().getVersioned();
    out.put("portfolioDoc", treeDoc);
    out.put("portfolio", treeDoc.getPortfolio());
    out.put("node", data().getNode());
    out.put("latestPositionDoc", latestPositionDoc);
    out.put("latestPosition", latestPositionDoc.getPosition());
    out.put("positionDoc", versionedPosition);
    out.put("position", versionedPosition.getPosition());
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
  public static URI uri(final WebPortfoliosData data, final UniqueIdentifier overrideVersionId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(null);
    String positionId = data.getBestPositionUriId(null);
    String versionId = (overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionVersionResource.class).build(portfolioId, nodeId, positionId, versionId);
  }

}
