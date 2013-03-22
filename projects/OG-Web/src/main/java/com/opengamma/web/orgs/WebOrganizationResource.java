/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.orgs;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;

/**
 * RESTful resource for an organization.
 */
@Path("/organizations/{organizationId}")
public class WebOrganizationResource extends AbstractWebOrganizationResource {
  
  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebOrganizationResource(final AbstractWebOrganizationResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "organization.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build(JSON_DIR + "organization.ftl", out);
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    OrganizationDocument doc = data().getOrganization();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getOrganizationMaster().remove(doc.getUniqueId());
    URI uri = WebOrganizationResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    OrganizationDocument doc = data().getOrganization();
    if (doc.isLatest()) {  // idempotent DELETE
      data().getOrganizationMaster().remove(doc.getUniqueId());
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
    OrganizationDocument organizationDoc = data().getOrganization();
    ManageableOrganization organization = organizationDoc.getOrganization();
    out.put("organizationDoc", organizationDoc); 
    out.put("organization", organization);
    out.put("deleted", !organizationDoc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebOrganizationVersionsResource findVersions() {
    return new WebOrganizationVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebOrganizationsData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideSecurityId  the override security id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebOrganizationsData data, final UniqueId overrideSecurityId) {
    String securityId = data.getBestOrganizationUriId(overrideSecurityId);
    return data.getUriInfo().getBaseUriBuilder().path(WebOrganizationResource.class).build(securityId);
  }

}
