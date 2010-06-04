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

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.id.UniqueIdentifier;
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
  public ManagablePositionMaster getPositionMaster() {
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
    Portfolio portfolio = getPositionMaster().getPortfolio(_portfolioUid);
    if (portfolio == null) {
      return null;
    }
    String html = "<html>" +
      "<head><title>Portfolio - " + portfolio.getUniqueIdentifier() + "</title></head>" +
      "<body>" +
      "<h2>Portfolio - " + portfolio.getUniqueIdentifier() + "</h2>" +
      "<p>" + portfolio.getName() + "</p>";
    html += "<p><table border=\"1\">" +
      "<tr><th>Name</th><th>Nodes</th><th>Positions</th><th>Actions</th></tr>";
    for (PortfolioNode node : portfolio.getRootNode().getChildNodes()) {
      URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioNodeResource.class).build(getPortfolioUid(), node.getUniqueIdentifier());
      html += "<tr>";
      html += "<td><a href=\"" + uri + "\">" + node.getName() + "</a></td>";
      html += "<td>" + node.getChildNodes().size() + "</td>";
      html += "<td>" + node.getPositions().size() + "</td>";
      html += "<td><br /></td>";
      html += "</tr>";
    }
    html += "</table>";
    
    html += "<h2>Update portfolio</h2>" +
      "<form method=\"POST\" action=\"" + getUriInfo().getAbsolutePath() + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(portfolio.getName()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>";
    html += "<h2>Delete portfolio</h2>" +
      "<form method=\"POST\" action=\"" + getUriInfo().getAbsolutePath() + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "<input type=\"hidden\" name=\"status\" value=\"D\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>";
    html += "</body></html>";
    return html;
  }

  @POST  // TODO: should be PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(@FormParam("name") String name, @FormParam("status") String status) {
    if ("D".equals(status)) {
      return remove();
    } else if ("A".equals(status)) {
      return reinstate();
    } else {
      return update(name);
    }
  }

  public Response update(String name) {
    PortfolioImpl portfolio = new PortfolioImpl(getPortfolioUid(), name);
    UniqueIdentifier uid = getPositionMaster().updatePortfolioOnly(portfolio);
    URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioResource.class).build(uid);
    return Response.seeOther(uri).build();
  }

  public Response remove() {
    getPositionMaster().removePortfolio(getPortfolioUid());
    URI uri = getUriInfo().getBaseUriBuilder().path(PortfoliosResource.class).build();
    return Response.seeOther(uri).build();
  }

  public Response reinstate() {
    UniqueIdentifier uid = getPositionMaster().reinstatePortfolio(getPortfolioUid());
    URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioResource.class).build(uid);
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

}
