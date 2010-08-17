/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.web;

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

import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioNodePositionsResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param data  the data, not null
   */
  public WebPortfolioNodePositionsResource(final WebPortfoliosData data) {
    super(data);
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("quantity") BigDecimal quantity,
      @FormParam("scheme") String scheme,
      @FormParam("schemevalue") String schemeValue) {
    scheme = StringUtils.trimToNull(scheme);
    schemeValue = StringUtils.trimToNull(schemeValue);
    if (quantity == null || scheme == null || schemeValue == null) {
      URI uri = WebPortfolioNodeResource.uri(data());
      String html = "<html>\n" +
        "<head><title>Add position</title></head>\n" +
        "<body>\n" +
        "<h2>Add position</h2>\n" +
        "<p>All details must be entered!</p>\n" +
        "<form method=\"POST\" action=\"" + uri + "\">" +
        "Quantity: <input type=\"text\" size=\"10\" name=\"quantity\" /><br />" +
        "Scheme: <input type=\"text\" size=\"30\" name=\"scheme\" /><br />" +
        "Scheme Id: <input type=\"text\" size=\"30\" name=\"schemevalue\" /><br />" +
        "<input type=\"submit\" value=\"Add\" />" +
        "</form>\n" +
        "</body>\n</html>\n";
      return Response.ok(html).build();
    }
    ManageablePosition position = new ManageablePosition(quantity, Identifier.of(scheme, schemeValue));
    PositionDocument doc = new PositionDocument(position, data().getNode().getUniqueIdentifier());
    doc = data().getPositionMaster().addPosition(doc);
    data().setPosition(doc);
    URI uri = WebPortfolioNodePositionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public WebPortfolioNodePositionResource findPosition(@PathParam("positionId") String idStr) {
    data().setUriPositionId(idStr);
    PositionDocument position = data().getPositionMaster().getPosition(UniqueIdentifier.parse(idStr));
    data().setPosition(position);
    return new WebPortfolioNodePositionResource(data());
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
