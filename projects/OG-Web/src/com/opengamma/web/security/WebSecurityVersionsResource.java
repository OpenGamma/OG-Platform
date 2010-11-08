/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityHistoryRequest;
import com.opengamma.financial.security.master.SecurityHistoryResult;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for all versions of a security.
 */
@Path("/securities/{securityId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebSecurityVersionsResource extends AbstractWebSecurityResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebSecurityVersionsResource(final AbstractWebSecurityResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    SecurityHistoryRequest request = new SecurityHistoryRequest();
    request.setSecurityId(data().getSecurity().getSecurityId());
    SecurityHistoryResult result = data().getSecurityMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getSecurities());
    return getFreemarker().build("securities/securityversions.ftl", out);
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
  @Path("{versionId}")
  public WebSecurityVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    SecurityDocument doc = data().getSecurity();
    UniqueIdentifier combined = doc.getSecurityId().withVersion(idStr);
    if (doc.getSecurityId().equals(combined) == false) {
      SecurityDocument versioned = data().getSecurityMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebSecurityVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data) {
    String securityId = data.getBestSecurityUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityVersionsResource.class).build(securityId);
  }

}
