/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.orgs;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an organization.
 */
@Path("/organizations/{organizationId}/versions")
public class WebOrganizationVersionsResource extends AbstractWebOrganizationResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebOrganizationVersionsResource(final AbstractWebOrganizationResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @Produces(MediaType.TEXT_HTML)
  @GET
  public String getHTML() {
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(data().getOrganization().getUniqueId());
    OrganizationHistoryResult result = data().getOrganizationMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getOrganizations());
    return getFreemarker().build(HTML_DIR + "organizationversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(data().getOrganization().getUniqueId());
    request.setPagingRequest(pr);
    OrganizationHistoryResult result = data().getOrganizationMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getOrganizations());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    String json = getFreemarker().build(JSON_DIR + "organizationversions.ftl", out);
    return Response.ok(json).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    OrganizationDocument doc = data().getOrganization();
    out.put("organizationDoc", doc);
    out.put("organization", doc.getOrganization());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebOrganizationVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    OrganizationDocument doc = data().getOrganization();
    UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      OrganizationDocument versioned = data().getOrganizationMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebOrganizationVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebOrganizationsData data) {
    String securityId = data.getBestOrganizationUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebOrganizationVersionsResource.class).build(securityId);
  }

}
