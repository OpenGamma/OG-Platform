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

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a node in a portfolio.
 */
@Path("/portfolios/{portfolioUid}/nodes/{nodeUid}")
public class PortfolioNodeResource {

  /**
   * The positions resource.
   */
  private final PortfolioNodesResource _nodesResource;
  /**
   * The position unique identifier.
   */
  private final UniqueIdentifier _nodeUid;

  /**
   * Creates the resource.
   * @param nodesResource  the parent resource, not null
   * @param nodeUid  the node unique identifier, not null
   */
  public PortfolioNodeResource(final PortfolioNodesResource nodesResource, final UniqueIdentifier nodeUid) {
    ArgumentChecker.notNull(nodesResource, "nodesResource");
    ArgumentChecker.notNull(nodeUid, "node");
    _nodesResource = nodesResource;
    _nodeUid = nodeUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the nodes resource.
   * @return the nodes resource, not null
   */
  public PortfolioNodesResource getPortfolioNodesResource() {
    return _nodesResource;
  }

  /**
   * Gets the node unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPortfolioNodeUid() {
    return _nodeUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPortfolioUid() {
    return getPortfolioNodesResource().getPortfolioResource().getPortfolioUid();
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public ManagablePositionMaster getPositionMaster() {
    return getPortfolioNodesResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPortfolioNodesResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml() {
    PortfolioNode node = getPositionMaster().getPortfolioNode(_nodeUid);
    if (node == null) {
      return null;
    }
    String html = "<html>" +
      "<head><title>Node - " + node.getUniqueIdentifier() + "</title></head>" +
      "<body>" +
      "<h2>Node - " + node.getUniqueIdentifier() + "</h2>" +
      "<p>" + node.getName() + "</p>" +
      "<h2>Positions</h2>" +
      "<p><table border=\"1\">";
    for (Position position : node.getPositions()) {
      URI uri = getUriInfo().getBaseUriBuilder().path(PositionResource.class).build(
          getPortfolioNodesResource().getPortfolioResource().getPortfolioUid(), position.getUniqueIdentifier());
      html += "<tr><td><a href=\"" + uri + "\">" + position.getUniqueIdentifier() + "</a></td></tr>";
    }
    html += "</table></p>";
    
    html += "<h2>Update node</h2>" +
      "<form method=\"POST\" action=\"" + getUriInfo().getAbsolutePath() + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(node.getName()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>";
    html += "<h2>Delete node</h2>" +
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
    PortfolioNodeImpl node = new PortfolioNodeImpl(getPortfolioNodeUid(), name);
    UniqueIdentifier uid = getPositionMaster().updatePortfolioNodeOnly(node);
    URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioNodeResource.class).build(getPortfolioUid(), uid);
    return Response.seeOther(uri).build();
  }

  public Response remove() {
    getPositionMaster().removePortfolioNode(getPortfolioNodeUid());
    URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioResource.class).build(getPortfolioUid());
    return Response.seeOther(uri).build();
  }

  public Response reinstate() {
    UniqueIdentifier uid = getPositionMaster().reinstatePortfolioNode(getPortfolioNodeUid());
    URI uri = getUriInfo().getBaseUriBuilder().path(PortfolioNodeResource.class).build(getPortfolioUid(), uid);
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("positions")
  public PositionsResource findPositions() {
    return new PositionsResource(getPortfolioNodesResource().getPortfolioResource());
  }

}
