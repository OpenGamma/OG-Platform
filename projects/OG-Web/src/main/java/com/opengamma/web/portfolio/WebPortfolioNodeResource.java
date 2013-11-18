/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * RESTful resource for a node in a portfolio.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}")
public class WebPortfolioNodeResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodeResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createPortfolioNodeData();
    return getFreemarker().build(HTML_DIR + "portfolionode.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON() {
    FlexiBean out = createPortfolioNodeData();
    PortfolioDocument doc = data().getPortfolio();
    if (!doc.isLatest()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    
    String s = getFreemarker().build(JSON_DIR + "portfolionode.ftl", out);
    return Response.ok(s).build();
  }

  private FlexiBean createPortfolioNodeData() {
    ManageablePortfolioNode node = data().getNode();
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setPositionObjectIds(node.getPositionIds());
    PositionSearchResult positionsResult = data().getPositionMaster().search(positionSearch);
    resolveSecurities(positionsResult.getPositions());
    
    FlexiBean out = createRootData();
    out.put("positionsResult", positionsResult);
    out.put("positions", positionsResult.getPositions());
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    if (name == null) {
      FlexiBean out = createRootData();
      out.put("err_nameMissing", true);
      String html = getFreemarker().build(HTML_DIR + "portfolionode-add.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = createPortfolioNode(name);
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    URI uri = createPortfolioNode(name);
    return Response.created(uri).build();
  }

  private URI createPortfolioNode(String name) {
    ManageablePortfolioNode newNode = new ManageablePortfolioNode(name);
    ManageablePortfolioNode node = data().getNode();
    node.addChildNode(newNode);
    PortfolioDocument doc = data().getPortfolio();
    doc = data().getPortfolioMaster().update(doc);
    URI uri = WebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    data().setPortfolio(doc);
    return uri;
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(@FormParam("name") String name) {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    name = StringUtils.trimToNull(name);
    if (name == null) {
      FlexiBean out = createRootData();
      out.put("err_nameMissing", true);
      String html = getFreemarker().build(HTML_DIR + "portfolionode-update.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = updatePortfolioNode(name, doc);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(@FormParam("name") String name) {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    name = StringUtils.trimToNull(name);
    updatePortfolioNode(name, doc);
    return Response.ok().build();
  }

  private URI updatePortfolioNode(String name, PortfolioDocument doc) {
    ManageablePortfolioNode node = data().getNode();
    URI uri = WebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    if (Objects.equal(node.getName(), name) == false) {
      node.setName(name);
      doc = data().getPortfolioMaster().update(doc);
      data().setPortfolio(doc);
    }
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    if (data().getParentNode() == null) {
      throw new IllegalArgumentException("Root node cannot be deleted");
    }
    if (data().getParentNode().removeNode(data().getNode().getUniqueId()) == false) {
      throw new DataNotFoundException("PortfolioNode not found: " + data().getNode().getUniqueId());
    }
    doc = data().getPortfolioMaster().update(doc);
    data().setPortfolio(doc);
    URI uri = WebPortfolioNodeResource.uri(data(), data().getParentNode().getUniqueId());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      if (data().getParentNode() == null) {
        throw new IllegalArgumentException("Root node cannot be deleted");
      }
      if (data().getParentNode().removeNode(data().getNode().getUniqueId()) == false) {
        throw new DataNotFoundException("PortfolioNode not found: " + data().getNode().getUniqueId());
      }
      doc = data().getPortfolioMaster().update(doc);
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  @Path("positions")
  public WebPortfolioNodePositionsResource findPositions() {
    return new WebPortfolioNodePositionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PortfolioDocument doc = data().getPortfolio();
    ManageablePortfolioNode node = data().getNode();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("parentNode", data().getParentNode());
    out.put("node", node);
    out.put("childNodes", node.getChildNodes());
    out.put("deleted", !doc.isLatest());
    out.put("pathNodes", getPathNodes(node));
    return out;
  }

  /**
   * Extracts the path from the root node to the current node, for web breadcrumb display.
   * The nodes are returned in a list, ordered from root to current node.
   * Not using findNodeStackByObjectId(), which traverses the tree exhaustively until it finds the required path.
   * @param node  the current node
   * @return      a list of <UniqueId, String> pairs denoting all nodes on the path from root to current node
   */
  protected List<ObjectsPair<UniqueId, String>> getPathNodes(ManageablePortfolioNode node) {
    LinkedList<ObjectsPair<UniqueId, String>> result = new LinkedList<ObjectsPair<UniqueId, String>>();

    ManageablePortfolioNode currentNode = node;
    while (currentNode != null) {
      result.addFirst(ObjectsPair.of(currentNode.getUniqueId(), currentNode.getName()));
      currentNode = (currentNode.getParentNodeId() == null) 
          ? null 
          : data().getPortfolioMaster().getNode(currentNode.getParentNodeId());       
    }
    return result;
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
  public static URI uri(final WebPortfoliosData data, final UniqueId overrideNodeId) {
    String portfolioId = data.getBestPortfolioUriId(null);
    String nodeId = data.getBestNodeUriId(overrideNodeId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodeResource.class).build(portfolioId, nodeId);
  }

}
