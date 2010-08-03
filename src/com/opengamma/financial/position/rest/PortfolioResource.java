/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;

import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a portfolio.
 */
@Path("/portfolios/{portfolioUid}")
public class PortfolioResource {

  /**
   * The portfolios resource.
   */
  private final PortfoliosResource _portfoliosResource;
  /**
   * The portfolio unique identifier.
   */
  private final UniqueIdentifier _portfolioUid;

  /**
   * Creates the resource.
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   */
  public PortfolioResource(final PortfoliosResource portfoliosResource, final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfoliosResource, "position master");
    ArgumentChecker.notNull(portfolioUid, "portfolio");
    _portfoliosResource = portfoliosResource;
    _portfolioUid = portfolioUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public PortfoliosResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the portfolio unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPortfolioUid() {
    return _portfolioUid;
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
  @Produces(FudgeRest.MEDIA)
  public PortfolioTreeDocument getAsFudge() {
    return getPositionMaster().getPortfolioTree(_portfolioUid);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml() {
    PortfolioTreeDocument doc = getPositionMaster().getPortfolioTree(_portfolioUid);
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
    for (PortfolioNode child : doc.getPortfolio().getRootNode().getChildNodes()) {
      URI nodeUri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), child.getUniqueIdentifier().toLatest());
      html += "<tr>";
      html += "<td><a href=\"" + nodeUri + "\">" + child.getName() + "</a></td>";
      html += "<td><a href=\"" + nodeUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    html += "<p>Positions:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Quantity</th><th>Actions</th></tr>\n";
    for (Position position : doc.getPortfolio().getRootNode().getPositions()) {
      URI positionUri = PositionResource.uri(getUriInfo(), getPortfolioUid(), position.getUniqueIdentifier().toLatest());
      html += "<tr>";
      html += "<td><a href=\"" + positionUri + "\">" + position.getUniqueIdentifier().toLatest() + "</a></td>";
      html += "<td>" + position.getQuantity() + "</td>";
      html += "<td><a href=\"" + positionUri + "\">View</a></td>";
      html += "</tr>\n";
    }
    html += "</table></p>\n";
    
    URI portfolioUri = PortfolioResource.uri(getUriInfo(), doc.getPortfolioId());
    html += "<h2>Update portfolio</h2>\n" +
      "<form method=\"POST\" action=\"" + portfolioUri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(doc.getPortfolio().getName()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>\n";
    html += "<h2>Delete portfolio</h2>\n" +
      "<form method=\"POST\" action=\"" + portfolioUri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "<input type=\"hidden\" name=\"status\" value=\"D\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>\n";
    
    URI rootNodeUri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), doc.getPortfolio().getRootNode().getUniqueIdentifier());
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
      "<a href=\"" + PortfoliosResource.uri(getUriInfo()) + "\">Portfolio search</a><br />" +
      "</p>";
    html += "</body>\n</html>\n";
    return html;
  }

  @POST  // TODO: should be PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(@FormParam("name") String name, @FormParam("status") String status) {
    if ("D".equals(status)) {
      return remove();
    } else {
      return update(name);
    }
  }

  public Response update(String name) {
    PortfolioTreeDocument doc = getPositionMaster().getPortfolioTree(getPortfolioUid());
    ((PortfolioImpl) doc.getPortfolio()).setName(name);
    doc = getPositionMaster().updatePortfolioTree(doc);
    URI uri = PortfolioResource.uri(getUriInfo(), doc.getPortfolioId().toLatest());
    return Response.seeOther(uri).build();
  }

  public Response remove() {
    getPositionMaster().removePortfolioTree(getPortfolioUid());
    URI uri = PortfoliosResource.uri(getUriInfo());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("nodes")
  public PortfolioNodesResource findNodes() {
    return new PortfolioNodesResource(this);
  }

  @Path("positions")
  public PositionsResource findPositions() {
    return new PositionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioUid) {
    return uriInfo.getBaseUriBuilder().path(PortfolioResource.class).build(portfolioUid);
  }

}
