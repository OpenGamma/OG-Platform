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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.sleepycat.je.DatabaseNotFoundException;

/**
 * RESTful resource for a node in a portfolio.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioNodeResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param data  the data, not null
   */
  public WebPortfolioNodeResource(final WebPortfoliosData data) {
    super(data);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    ManageablePortfolioNode node = data().getNode();
    String html = "<html>\n" +
      "<head><title>Node - " + node.getUniqueIdentifier().toLatest() + "</title></head>\n" +
      "<body>\n" +
      "<h2>Node - " + node.getUniqueIdentifier().toLatest() + "</h2>" +
      "<p>Name: " + node.getName() + "<br />\n" +
      "Version: " + node.getUniqueIdentifier().getVersion() + "</p>\n";
    
    html += "<p>Child nodes:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Actions</th></tr>\n";
    for (ManageablePortfolioNode child : node.getChildNodes()) {
      URI nodeUri = WebPortfolioNodeResource.uri(data(), child.getUniqueIdentifier());
      html += "<tr>";
      html += "<td><a href=\"" + nodeUri + "\">" + child.getName() + "</a></td>";
      html += "<td><a href=\"" + nodeUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setParentNodeId(node.getUniqueIdentifier());
    PositionSearchResult positions = data().getPositionMaster().searchPositions(positionSearch);
    html += "<p>Positions:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Quantity</th><th>Actions</th></tr>\n";
    for (PositionDocument position : positions.getDocuments()) {
      URI positionUri = WebPortfolioNodePositionResource.uri(data(), position.getPositionId());
      html += "<tr>";
      html += "<td><a href=\"" + positionUri + "\">" + position.getPositionId().toLatest() + "</a></td>";
      html += "<td>" + position.getPosition().getQuantity() + "</td>";
      html += "<td><a href=\"" + positionUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    
    URI uri = WebPortfolioNodeResource.uri(data());
    html += "<h2>Update node</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(node.getName()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>\n";
    html += "<h2>Delete node</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"DELETE\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>\n";
    html += "<h2>Add node</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    URI uriPositions = WebPortfolioNodePositionsResource.uri(data());
    html += "<h2>Add position</h2>\n" +
      "<form method=\"POST\" action=\"" + uriPositions + "\">" +
      "Quantity: <input type=\"text\" size=\"10\" name=\"quantity\" /><br />" +
      "Scheme: <input type=\"text\" size=\"30\" name=\"scheme\" /><br />" +
      "Scheme Id: <input type=\"text\" size=\"30\" name=\"schemevalue\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    
    html += "<h2>Links</h2>\n" +
      "<p>";
    if (data().getParentNode() != null) {
      html += "<a href=\"" + WebPortfolioNodeResource.uri(data(), data().getParentNode().getUniqueIdentifier()) + "\">Parent node</a><br />";
    }
    html += "<a href=\"" + WebPortfolioResource.uri(data()) + "\">Portfolio</a><br />" +
      "<a href=\"" + WebPortfoliosResource.uri(data()) + "\">Portfolio search</a><br />" +
      "</p>";
    html += "</body>\n</html>\n";
    return html;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    if (name == null) {
      URI uri = WebPortfolioNodeResource.uri(data());
      String html = "<html>\n" +
        "<head><title>Add node</title></head>\n" +
        "<body>\n" +
        "<h2>Add node</h2>\n" +
        "<p>The name must be entered!</p>\n" +
        "<form method=\"POST\" action=\"" + uri + "\">" +
        "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(name) + "\" /><br />" +
        "<input type=\"submit\" value=\"Add\" />" +
        "</form>\n" +
        "</body>\n</html>\n";
      return Response.ok(html).build();
    }
    ManageablePortfolioNode node = data().getNode();
    ManageablePortfolioNode newNode = new ManageablePortfolioNode(StringUtils.trim(name));
    node.addChildNode(newNode);
    PortfolioTreeDocument doc = data().getPortfolio();
    doc = data().getPositionMaster().updatePortfolioTree(doc);
    URI uri = WebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    data().setPortfolio(doc);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    PortfolioTreeDocument doc = data().getPortfolio();
    ManageablePortfolioNode node = data().getNode();
    node.setName(StringUtils.trim(name));
    doc = data().getPositionMaster().updatePortfolioTree(doc);
    URI uri = WebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    data().setPortfolio(doc);
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    if (data().getParentNode() == null) {
      throw new IllegalArgumentException("Root node cannot be deleted");
    }
    PortfolioTreeDocument doc = data().getPortfolio();
    if (data().getParentNode().removeNode(data().getNode().getUniqueIdentifier()) == false) {
      throw new DatabaseNotFoundException("PortfolioNode not found: " + data().getNode().getUniqueIdentifier());
    }
    doc = data().getPositionMaster().updatePortfolioTree(doc);
    data().setPortfolio(doc);
    URI uri = WebPortfolioNodeResource.uri(data(), data().getParentNode().getUniqueIdentifier());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("positions")
  public WebPortfolioNodePositionsResource findPositions() {
    return new WebPortfolioNodePositionsResource(data());
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
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodeResource.class).build(portfolioId, nodeId);
  }

}
