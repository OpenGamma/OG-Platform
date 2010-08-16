/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;

import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a portfolio.
 */
@Path("/portfolios/{portfolioUid}")
public class WebPortfolioResource {

  /**
   * The portfolios resource.
   */
  private final WebPortfoliosResource _portfoliosResource;
  /**
   * The portfolio unique identifier.
   */
  private final UniqueIdentifier _urlPortfolioId;

  /**
   * Creates the resource.
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   */
  public WebPortfolioResource(final WebPortfoliosResource portfoliosResource, final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfoliosResource, "position master");
    ArgumentChecker.notNull(portfolioUid, "portfolio");
    _portfoliosResource = portfoliosResource;
    _urlPortfolioId = portfolioUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public WebPortfoliosResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the portfolio identifier from the URL.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUrlPortfolioId() {
    return _urlPortfolioId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getPortfoliosResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPortfoliosResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml() {
    PortfolioTreeDocument doc = getPositionMaster().getPortfolioTree(getUrlPortfolioId());
    if (doc == null) {
      return null;
    }
    String html = "<html>\n" +
      "<head><title>Portfolio - " + doc.getPortfolioId().toLatest() + "</title></head>\n" +
      "<body>\n" +
      "<h2>Portfolio - " + doc.getPortfolioId().toLatest() + "</h2>\n" +
      "<p>Name: " + doc.getPortfolio().getName() + "<br />\n" +
      "Version: " + doc.getPortfolioId().getVersion() + "</p>\n";
    
    html += "<p>Child nodes:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Actions</th></tr>\n";
    for (ManageablePortfolioNode child : doc.getPortfolio().getRootNode().getChildNodes()) {
      URI nodeUri = WebPortfolioNodeResource.uri(getUriInfo(), getUrlPortfolioId(), child.getUniqueIdentifier().toLatest());
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
    PositionSearchResult positions = getPositionMaster().searchPositions(positionSearch);
    for (ManageablePosition position : positions.getPositions()) {
      URI positionUri = WebPositionResource.uri(getUriInfo(), getUrlPortfolioId(), position.getUniqueIdentifier().toLatest());
      html += "<tr>";
      html += "<td><a href=\"" + positionUri + "\">" + position.getUniqueIdentifier().toLatest() + "</a></td>";
      html += "<td>" + position.getQuantity() + "</td>";
      html += "<td><a href=\"" + positionUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    
    URI portfolioUri = WebPortfolioResource.uri(getUriInfo(), doc.getPortfolioId());
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
    
    URI rootNodeUri = WebPortfolioNodeResource.uri(getUriInfo(), getUrlPortfolioId(), doc.getPortfolio().getRootNode().getUniqueIdentifier());
    html += "<h2>Add node</h2>\n" +
      "<form method=\"POST\" action=\"" + rootNodeUri + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    html += "<h2>Add position</h2>\n" +
      "<form method=\"POST\" action=\"" + rootNodeUri + "\">" +
      "<input type=\"hidden\" name=\"post\" value=\"P\" /><br />" +
      "Quantity: <input type=\"text\" size=\"10\" name=\"quantity\" /><br />" +
      "Scheme: <input type=\"text\" size=\"30\" name=\"scheme\" /><br />" +
      "Scheme Id: <input type=\"text\" size=\"30\" name=\"schemevalue\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    
    html += "<h2>Links</h2>" +
      "<p>" +
      "<a href=\"" + WebPortfoliosResource.uri(getUriInfo()) + "\">Portfolio search</a><br />" +
      "</p>";
    html += "</body>\n</html>\n";
    return html;
  }

  @PUT
  public Response update(@FormParam("name") String name) {
    PortfolioTreeDocument doc = getPositionMaster().getPortfolioTree(getUrlPortfolioId());
    doc.getPortfolio().setName(name);
    doc = getPositionMaster().updatePortfolioTree(doc);
    URI uri = WebPortfolioResource.uri(getUriInfo(), doc.getPortfolioId().toLatest());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    getPositionMaster().removePortfolioTree(getUrlPortfolioId());
    URI uri = WebPortfoliosResource.uri(getUriInfo());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("nodes")
  public WebPortfolioNodesResource findNodes() {
    return new WebPortfolioNodesResource(this);
  }

  @Path("positions")
  public WebPositionsResource findPositions() {
    return new WebPositionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioUid) {
    return uriInfo.getBaseUriBuilder().path(WebPortfolioResource.class).build(portfolioUid);
  }

}
