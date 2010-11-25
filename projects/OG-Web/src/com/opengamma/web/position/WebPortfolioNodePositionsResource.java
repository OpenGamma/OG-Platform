/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PositionDocument;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions")
@Produces(MediaType.TEXT_HTML)
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
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("quantity") String quantityStr,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    quantityStr = StringUtils.trimToNull(quantityStr);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    if (quantityStr == null || idScheme == null || idValue == null) {
      FlexiBean out = createRootData();
      if (quantityStr == null) {
        out.put("err_quantityMissing", true);
      }
      if (idScheme == null) {
        out.put("err_idschemeMissing", true);
      }
      if (idValue == null) {
        out.put("err_idvalueMissing", true);
      }
      String html = getFreemarker().build("portfolios/portfolionodepositions-add.ftl", out);
      return Response.ok(html).build();
    }
    ManageablePosition position = new ManageablePosition(new BigDecimal(quantityStr), Identifier.of(idScheme, idValue));
    PositionDocument doc = new PositionDocument(position, data().getNode().getUniqueIdentifier());
    doc = data().getPositionMaster().addPosition(doc);
    data().setPosition(doc);
    URI uri = WebPortfolioNodePositionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioTreeDocument doc = data().getPortfolio();
    ManageablePortfolioNode node = data().getNode();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("parentNode", data().getParentNode());
    out.put("node", node);
    out.put("childNodes", node.getChildNodes());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public WebPortfolioNodePositionResource findPosition(@PathParam("positionId") String idStr) {
    data().setUriPositionId(idStr);
    PositionDocument position = data().getPositionMaster().getPosition(UniqueIdentifier.parse(idStr));
    data().setPosition(position);
    return new WebPortfolioNodePositionResource(this);
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
