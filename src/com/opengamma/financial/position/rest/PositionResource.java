/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.math.BigDecimal;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.ManagedPosition;
import com.opengamma.financial.position.UpdatePositionRequest;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a postion in a portfolio.
 */
@Path("/portfolios/{portfolioUid}/positions/{positionUid}")
public class PositionResource {

  /**
   * The positions resource.
   */
  private final PositionsResource _positionsResource;
  /**
   * The position unique identifier.
   */
  private final UniqueIdentifier _positionUid;

  /**
   * Creates the resource.
   * @param positionsResource  the parent resource, not null
   * @param positionUid  the position unique identifier, not null
   */
  public PositionResource(final PositionsResource positionsResource, final UniqueIdentifier positionUid) {
    ArgumentChecker.notNull(positionsResource, "PositionsResource");
    ArgumentChecker.notNull(positionUid, "position");
    _positionsResource = positionsResource;
    _positionUid = positionUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the positions resource.
   * @return the positions resource, not null
   */
  public PositionsResource getPositionsResource() {
    return _positionsResource;
  }

  /**
   * Gets the position unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPositionUid() {
    return _positionUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getPortfolioUid() {
    return getPositionsResource().getPortfolioUid();
  }

  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public ManagablePositionMaster getPositionMaster() {
    return getPositionsResource().getPositionMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getPositionsResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getAsHtml() {
    ManagedPosition position = getPositionMaster().getManagedPosition(_positionUid);
    if (position == null) {
      return null;
    }
    String html = "<html>" +
      "<head><title>Position - " + position.getUniqueIdentifier().toLatest() + "</title></head>" +
      "<body>" +
      "<h2>Position - " + position.getUniqueIdentifier().toLatest() + "</h2>" +
      "<p>" +
      "Version: " + position.getUniqueIdentifier().getVersion() + "<br />" +
      "Quantity: " + position.getQuantity() + "<br />" +
      "Security: " + position.getSecurityKey() + "</p>";
    
    URI uri = PositionResource.uri(getUriInfo(), getPortfolioUid(), position.getUniqueIdentifier());
    Identifier identifier = position.getSecurityKey().getIdentifiers().iterator().next();
    html += "<h2>Update position</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "Quantity: <input type=\"text\" size=\"10\" name=\"quantity\" value=\"" + StringEscapeUtils.escapeHtml(position.getQuantity().toPlainString()) + "\" /><br />" +
      "Scheme: <input type=\"text\" size=\"30\" name=\"scheme\" value=\"" + StringEscapeUtils.escapeHtml(identifier.getScheme().getName()) + "\" /><br />" +
      "Scheme Id: <input type=\"text\" size=\"30\" name=\"schemevalue\" value=\"" + StringEscapeUtils.escapeHtml(identifier.getValue()) + "\" /><br />" +
      "<input type=\"submit\" value=\"Update\" />" +
      "</form>\n";
    html += "<h2>Delete position</h2>\n" +
      "<form method=\"POST\" action=\"" + uri + "\">" +
      "<input type=\"hidden\" name=\"method\" value=\"PUT\" />" +
      "<input type=\"hidden\" name=\"status\" value=\"D\" />" +
      "<input type=\"submit\" value=\"Delete\" />" +
      "</form>\n";
    
    html += "<h2>Links</h2>\n" +
      "<p>" +
      "<a href=\"" + PortfolioNodeResource.uri(getUriInfo(), position.getPortfolioUid(), position.getParentNodeUid().toLatest()) + "\">Parent node</a><br />" +
      "<a href=\"" + PortfolioResource.uri(getUriInfo(), position.getPortfolioUid().toLatest()) + "\">Portfolio</a><br />" +
      "<a href=\"" + PortfoliosResource.uri(getUriInfo()) + "\">Portfolio search</a><br />" +
      "</body>" +
      "</html>";
    return html;
  }

  @POST  // TODO: should be PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(@FormParam("method") String method, @FormParam("status") String status,
      @FormParam("quantity") String quantity, @FormParam("scheme") String scheme, @FormParam("schemevalue") String schemeValue) {
    if ("PUT".equals(method)) {
      if ("D".equals(status)) {
        return remove();
      } else if ("A".equals(status)) {
        return reinstate();
      } else {
        return update(new BigDecimal(quantity), StringUtils.trim(scheme), StringUtils.trim(schemeValue));
      }
    }
    return Response.status(Status.BAD_REQUEST).build();
  }

  public Response update(BigDecimal quantity, String scheme, String schemeValue) {
    UpdatePositionRequest request = new UpdatePositionRequest();
    request.setUniqueIdentifier(getPositionUid());
    request.setQuantity(quantity);
    request.setSecurityKey(new IdentifierBundle(Identifier.of(scheme, schemeValue)));
    UniqueIdentifier uid = getPositionMaster().updatePosition(request);
    URI uri = PositionResource.uri(getUriInfo(), getPortfolioUid(), uid.toLatest());
    return Response.seeOther(uri).build();
  }

  public Response remove() {
    ManagedPosition position = getPositionMaster().getManagedPosition(getPositionUid());
    getPositionMaster().removePosition(getPositionUid());
    URI uri = PortfolioNodeResource.uri(getUriInfo(), getPortfolioUid(), position.getParentNodeUid().toLatest());
    return Response.seeOther(uri).build();
  }

  public Response reinstate() {
    UniqueIdentifier positionUid = getPositionMaster().reinstatePosition(getPositionUid());
    URI uri = PositionResource.uri(getUriInfo(), getPortfolioUid(), positionUid.toLatest());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * @param uriInfo  the URI information, not null
   * @param portfolioUid  the portfolio unique identifier, not null
   * @param positionUid  the position unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier portfolioUid, UniqueIdentifier positionUid) {
    return uriInfo.getBaseUriBuilder().path(PositionResource.class).build(portfolioUid.toLatest(), positionUid);
  }

}
