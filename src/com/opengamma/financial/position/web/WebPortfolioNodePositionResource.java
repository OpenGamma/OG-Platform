/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.web;

import java.math.BigDecimal;
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

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for a position in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions/{positionId}")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioNodePositionResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodePositionResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("portfolios/portfolionodeposition.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("quantity") String quantityStr,
      @FormParam("scheme") String scheme,
      @FormParam("schemevalue") String schemeValue) {
    quantityStr = StringUtils.trimToNull(quantityStr);
    scheme = StringUtils.trimToNull(scheme);
    schemeValue = StringUtils.trimToNull(schemeValue);
    if (quantityStr == null || scheme == null || schemeValue == null) {
      FlexiBean out = createRootData();
      if (quantityStr == null) {
        out.put("err_quantityMissing", true);
      }
      if (scheme == null) {
        out.put("err_schemeMissing", true);
      }
      if (schemeValue == null) {
        out.put("err_schemevalueMissing", true);
      }
      String html = getFreemarker().build("portfolios/portfolionodeposition-update.ftl", out);
      return Response.ok(html).build();
    }
    PositionDocument doc = data().getPosition();
    ManageablePosition position = doc.getPosition();
    position.setQuantity(new BigDecimal(quantityStr));
    position.setSecurityKey(new IdentifierBundle(Identifier.of(scheme, schemeValue)));
    doc = data().getPositionMaster().updatePosition(doc);
    data().setPosition(doc);
    URI uri = WebPortfolioNodePositionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    PositionDocument doc = data().getPosition();
    data().getPositionMaster().removePosition(doc.getPositionId());
    URI uri = WebPortfolioNodeResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  public FlexiBean createRootData() {
    PortfolioTreeDocument treeDoc = data().getPortfolio();
    PositionDocument positionDoc = data().getPosition();
    FlexiBean out = getFreemarker().createRootData();
    out.put("portfolioDoc", treeDoc);
    out.put("portfolio", treeDoc.getPortfolio());
    out.put("node", data().getNode());
    out.put("positionDoc", positionDoc);
    out.put("position", positionDoc.getPosition());
    out.put("uris", new WebPortfoliosUris(data()));
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
   * @param overridePositionId  the override position id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueIdentifier overridePositionId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(null);
    String positionId = data.getBestPositionUriId(overridePositionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionResource.class).build(portfolioId, nodeId, positionId);
  }

}
