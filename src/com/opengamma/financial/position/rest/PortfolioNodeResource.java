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
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.AddPortfolioNodeRequest;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.UpdatePortfolioNodeRequest;
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
    String html = "<html>\n" +
      "<head><title>Node - " + node.getUniqueIdentifier().toLatest() + "</title></head>\n" +
      "<body>\n" +
      "<h2>Node - " + node.getUniqueIdentifier().toLatest() + "</h2>" +
      "<p>Name: " + node.getName() + "<br />\n" +
      "Version: " + node.getUniqueIdentifier().getVersion() + "</p>\n";
    html += "<p>Child nodes:<br /><table border=\"1\">" +
      "<tr><th>Name</th><th>Nodes</th><th>Positions</th><th>Actions</th></tr>";
    for (PortfolioNode child : node.getChildNodes()) {
      URI nodeUri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), child.getUniqueIdentifier().toLatest());
      html += "<tr>";
      html += "<td><a href=\"" + nodeUri + "\">" + child.getName() + "</a></td>";
      html += "<td>" + child.getChildNodes().size() + "</td>";
      html += "<td>" + child.getPositions().size() + "</td>";
      html += "<td><br /></td>";
      html += "</tr>";
    }
    html += "</table></p>\n";
    html += "<p>Positions:<br /><table border=\"1\">";
    for (Position position : node.getPositions()) {
      URI positionUri = PositionResource.uri(getUriInfo(), getPortfolioUid(), position.getUniqueIdentifier().toLatest());
      html += "<tr><td><a href=\"" + positionUri + "\">" + position.getUniqueIdentifier().toLatest() + "</a></td></tr>";
    }
    html += "</table></p>\n";
    
    URI uri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), node.getUniqueIdentifier());
    html += "<h2>Update node</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" value=\"" + StringEscapeUtils.escapeHtml(node.getName()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>\n";
    html += "<h2>Delete node</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "<input type=\"hidden\" name=\"status\" value=\"D\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>\n";
    html += "<h2>Add node</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "Name: <input type=\"text\" size=\"30\" name=\"name\" /><br />" +
      "<input type=\"submit\" value=\"Add\" />" +
      "</form>\n";
    
    html += "<h2>Links</h2>" +
      "<p>" +
      "<a href=\"" + PortfolioResource.uri(getUriInfo(), getPortfolioUid().toLatest()) + "\">Portfolio</a><br />" +
      "<a href=\"" + PortfoliosResource.uri(getUriInfo()) + "\">Portfolio search</a><br />" +
      "</p>";
//      "<a href=\"" + PortfolioNodeResource.uri(getUriInfo(), summary.getPortfolioUid(), summary.getParentNodeUid().toLatest()) + "\">Parent node</a><br />";
    html += "</body>\n</html>\n";
    return html;
  }

  @POST  // TODO: should be PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(@FormParam("method") String method, @FormParam("name") String name, @FormParam("status") String status) {
    if ("PUT".equals(method)) {
      if ("D".equals(status)) {
        return remove();
      } else if ("A".equals(status)) {
        return reinstate();
      } else {
        return update(name);
      }
    }
    return add(name);
  }

  public Response add(String name) {
    if (name == null) {
      URI uri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), getPortfolioNodeUid());
      String html = "<html>\n" +
        "<head><title>Portfolios</title></head>\n" +
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
    AddPortfolioNodeRequest request = new AddPortfolioNodeRequest();
    request.setParentNode(getPortfolioNodeUid());
    request.setName(name);
    UniqueIdentifier uid = getPositionMaster().addPortfolioNode(request);
    URI uri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), uid.toLatest());
    return Response.seeOther(uri).build();
  }

  public Response update(String name) {
    UpdatePortfolioNodeRequest request = new UpdatePortfolioNodeRequest();
    request.setUniqueIdentifier(getPortfolioNodeUid());
    request.setName(name);
    UniqueIdentifier uid = getPositionMaster().updatePortfolioNode(request);
    URI uri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), uid.toLatest());
    return Response.seeOther(uri).build();
  }

  public Response remove() {
    getPositionMaster().removePortfolioNode(getPortfolioNodeUid());
    URI uri = PortfolioResource.uri(getUriInfo(), getPortfolioUid().toLatest());
    return Response.seeOther(uri).build();
  }

  public Response reinstate() {
    UniqueIdentifier uid = getPositionMaster().reinstatePortfolioNode(getPortfolioNodeUid());
    URI uri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), uid.toLatest());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("positions")
  public PositionsResource findPositions() {
    return new PositionsResource(getPortfolioNodesResource().getPortfolioResource());
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   * @param nodeUid  the node unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioUid, UniqueIdentifier nodeUid) {
    return uriInfo.getBaseUriBuilder().path(PortfolioNodeResource.class).build(portfolioUid.toLatest(), nodeUid);
  }

}
