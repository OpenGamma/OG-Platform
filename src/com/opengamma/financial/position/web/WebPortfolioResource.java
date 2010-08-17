/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.web;

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

import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;

/**
 * RESTful resource for a portfolio.
 */
@Path("/portfolios/{portfolioId}")
public class WebPortfolioResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param data  the data, not null
   */
  public WebPortfolioResource(final WebPortfoliosData data) {
    super(data);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    PortfolioTreeDocument doc = data().getPortfolio();
    String html = "<html>\n" +
      "<head><title>Portfolio - " + doc.getPortfolioId().toLatest() + "</title></head>\n" +
      "<body>\n" +
      "<h2>Portfolio - " + doc.getPortfolioId().toLatest() + "</h2>\n" +
      "<p>Name: " + doc.getPortfolio().getName() + "<br />\n" +
      "Version: " + doc.getPortfolioId().getVersion() + "</p>\n";
    
    html += "<p>Child nodes:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Actions</th></tr>\n";
    for (ManageablePortfolioNode child : doc.getPortfolio().getRootNode().getChildNodes()) {
      URI nodeUri = WebPortfolioNodeResource.uri(data(), child.getUniqueIdentifier());
      html += "<tr>";
      html += "<td><a href=\"" + nodeUri + "\">" + child.getName() + "</a></td>";
      html += "<td><a href=\"" + nodeUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    
    html += "<p>Positions:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Quantity</th><th>Actions</th></tr>\n";
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setParentNodeId(doc.getPortfolio().getRootNode().getUniqueIdentifier());
    PositionSearchResult positions = data().getPositionMaster().searchPositions(positionSearch);
    for (ManageablePosition position : positions.getPositions()) {
      URI positionUri = WebPortfolioNodePositionResource.uri(data(), position.getUniqueIdentifier());
      html += "<tr>";
      html += "<td><a href=\"" + positionUri + "\">" + position.getUniqueIdentifier().toLatest() + "</a></td>";
      html += "<td>" + position.getQuantity() + "</td>";
      html += "<td><a href=\"" + positionUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    
    URI portfolioUri = WebPortfolioResource.uri(data());
    html += "<h2>Update portfolio</h2>\n" +
      "<form method=\"POST\" action=\"" + portfolioUri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(doc.getPortfolio().getName()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>\n";
    html += "<h2>Delete portfolio</h2>\n" +
      "<form method=\"POST\" action=\"" + portfolioUri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"DELETE\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>\n";
    
    URI rootNodeUri = WebPortfolioNodeResource.uri(data(), doc.getPortfolio().getRootNode().getUniqueIdentifier());
    html += "<h2>Add node</h2>\n" +
      "<form method=\"POST\" action=\"" + rootNodeUri + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    URI rootNodePositionsUri = WebPortfolioNodePositionsResource.uri(data(), doc.getPortfolio().getRootNode().getUniqueIdentifier());
    html += "<h2>Add position</h2>\n" +
      "<form method=\"POST\" action=\"" + rootNodePositionsUri + "\">" +
      "Quantity: <input type=\"text\" size=\"10\" name=\"quantity\" /><br />" +
      "Scheme: <input type=\"text\" size=\"30\" name=\"scheme\" /><br />" +
      "Scheme Id: <input type=\"text\" size=\"30\" name=\"schemevalue\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    
    html += "<h2>Links</h2>" +
      "<p>" +
      "<a href=\"" + WebPortfoliosResource.uri(data()) + "\">Portfolio search</a><br />" +
      "</p>";
    html += "</body>\n</html>\n";
    return html;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    PortfolioTreeDocument doc = data().getPortfolio();
    doc.getPortfolio().setName(StringUtils.trim(name));
    doc = data().getPositionMaster().updatePortfolioTree(doc);
    data().setPortfolio(doc);
    URI uri = WebPortfolioResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    PortfolioTreeDocument doc = data().getPortfolio();
    data().getPositionMaster().removePortfolioTree(doc.getPortfolioId());
    URI uri = WebPortfoliosResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("nodes")
  public WebPortfolioNodesResource findNodes() {
    return new WebPortfolioNodesResource(data());
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    String portfolioId = data.getBestPortfolioUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioResource.class).build(portfolioId);
  }

}
