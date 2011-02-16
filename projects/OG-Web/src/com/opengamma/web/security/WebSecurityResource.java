/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;

/**
 * RESTful resource for a security.
 */
@Path("/securities/{securityId}")
public class WebSecurityResource extends AbstractWebSecurityResource {
  
  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebSecurityResource(final AbstractWebSecurityResource parent) {
    super(parent);
  }
  
  //Return the security as JSON document
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSONSecurity() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/jsonsecurity.ftl", out);
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/security.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response put(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(get()).build();
    }
    URI uri = updateSecurity(doc);
    return Response.seeOther(uri).build();
  }
  
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(get()).build();
    }
    updateSecurity(doc);
    return Response.ok().build();
  }

  private URI updateSecurity(SecurityDocument doc) {
    IdentifierBundle identifierBundle = doc.getSecurity().getIdentifiers();
    data().getSecurityLoader().loadSecurity(Collections.singleton(identifierBundle));
    URI uri = WebSecurityResource.uri(data());
    return uri;
  }

  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response delete() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(get()).build();
    }
    
    data().getSecurityMaster().remove(doc.getUniqueId());
    URI uri = WebSecurityResource.uri(data());
    return Response.seeOther(uri).build();
  }
  
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest()) {
      data().getSecurityMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    SecurityDocument doc = data().getSecurity();
    out.put("securityDoc", doc);
    out.put("security", doc.getSecurity());
    out.put("deleted", !doc.isLatest());
    addSecuritySpecificMetaData(doc.getSecurity(), out);
    return out;
  }

  /**
   * @param security
   * @param out
   */
  private void addSecuritySpecificMetaData(ManageableSecurity security, FlexiBean out) {
    if (security.getSecurityType().equals("SWAP")) {
      SwapSecurity swapSecurity = (SwapSecurity) security;
      out.put("payLegType", swapSecurity.getPayLeg().accept(new SwapLegClassifierVisitor()));
      out.put("receiveLegType", swapSecurity.getReceiveLeg().accept(new SwapLegClassifierVisitor()));
    }
  }

  private class SwapLegClassifierVisitor implements SwapLegVisitor<String> {

    @Override
    public String visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return "FixedInterestRateLeg";
    }

    @Override
    public String visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return "FloatingInterestRateLeg";
    }
  }
  
  //-------------------------------------------------------------------------
  @Path("versions")
  public WebSecurityVersionsResource findVersions() {
    return new WebSecurityVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideSecurityId  the override security id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data, final UniqueIdentifier overrideSecurityId) {
    String securityId = data.getBestSecurityUriId(overrideSecurityId);
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityResource.class).build(securityId);
  }

}
