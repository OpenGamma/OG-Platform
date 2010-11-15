/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.engine.security.ManageableSecurity;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

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

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/security.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    
    SecurityDocument doc = data().getSecurity();
    
    IdentifierBundle identifierBundle = doc.getSecurity().getIdentifiers();
    Map<IdentifierBundle, ManageableSecurity> loadedSecurities = data().getSecurityLoader().loadSecurity(Collections.singleton(identifierBundle));
    ManageableSecurity updatedSecurity = loadedSecurities.get(identifierBundle);
    SecurityDocument updateDoc = new SecurityDocument();
    
    UniqueIdentifier securityId = doc.getSecurityId();
    updateDoc.setSecurityId(securityId);
    updateDoc.setSecurity(updatedSecurity);
    data().getSecurityMaster().update(updateDoc);
    
    URI uri = WebSecurityResource.uri(data());
    return Response.seeOther(uri).build();
   
  }

  @DELETE
  public Response delete() {
    SecurityDocument doc = data().getSecurity();
    data().getSecurityMaster().remove(doc.getSecurityId());
    URI uri = WebSecuritiesResource.uri(data());
    return Response.seeOther(uri).build();
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
    return out;
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
    String portfolioId = data.getBestSecurityUriId(overrideSecurityId);
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityResource.class).build(portfolioId);
  }

}
