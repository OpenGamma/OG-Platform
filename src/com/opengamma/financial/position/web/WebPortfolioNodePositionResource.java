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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.financial.position.master.ManageablePosition;
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
   * @param data  the data, not null
   */
  public WebPortfolioNodePositionResource(final WebPortfoliosData data) {
    super(data);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getAsHtml() {
    PositionDocument doc = data().getPosition();
    String html = "<html>" +
      "<head><title>Position - " + doc.getPositionId().toLatest() + "</title></head>" +
      "<body>" +
      "<h2>Position - " + doc.getPositionId().toLatest() + "</h2>" +
      "<p>" +
      "Version: " + doc.getPositionId().getVersion() + "<br />" +
      "Quantity: " + doc.getPosition().getQuantity() + "<br />" +
      "Security: " + doc.getPosition().getSecurityKey() + "</p>";
    
    URI uri = WebPortfolioNodePositionResource.uri(data());
    Identifier identifier = doc.getPosition().getSecurityKey().getIdentifiers().iterator().next();
    html += "<h2>Update position</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Quantity: <input type=\"text\" size=\"10\" name=\"quantity\" value=\"" + StringEscapeUtils.escapeHtml(doc.getPosition().getQuantity().toPlainString()) + "\" /><br />" +
      "Scheme: <input type=\"text\" size=\"30\" name=\"scheme\" value=\"" + StringEscapeUtils.escapeHtml(identifier.getScheme().getName()) + "\" /><br />" +
      "Scheme Id: <input type=\"text\" size=\"30\" name=\"schemevalue\" value=\"" + StringEscapeUtils.escapeHtml(identifier.getValue()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>\n";
    html += "<h2>Delete position</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"DELETE\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>\n";
    
    html += "<h2>Links</h2>\n" +
      "<p>" +
      "<a href=\"" + WebPortfolioNodeResource.uri(data()) + "\">Parent node</a><br />" +
      "<a href=\"" + WebPortfolioResource.uri(data()) + "\">Portfolio</a><br />" +
      "<a href=\"" + WebPortfoliosResource.uri(data()) + "\">Portfolio search</a><br />" +
      "</body>" +
      "</html>";
    return html;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("quantity") BigDecimal quantity,
      @FormParam("scheme") String scheme,
      @FormParam("schemevalue") String schemeValue) {
    scheme = StringUtils.trimToNull(scheme);
    schemeValue = StringUtils.trimToNull(schemeValue);
    PositionDocument doc = data().getPosition();
    ManageablePosition position = doc.getPosition();
    position.setQuantity(quantity);
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
