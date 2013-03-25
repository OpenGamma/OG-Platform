/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * RESTful resource for a organization.
 */
public class DataOrganizationResource
    extends AbstractDocumentDataResource<OrganizationDocument> {

  /**
   * The organizations resource.
   */
  private final DataOrganizationMasterResource _organizationsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   */
  DataOrganizationResource() {
    _organizationsResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param organizationsResource the parent resource, not null
   * @param organizationId        the organization unique identifier, not null
   */
  public DataOrganizationResource(final DataOrganizationMasterResource organizationsResource, final ObjectId organizationId) {
    ArgumentChecker.notNull(organizationsResource, "organizationsResource");
    ArgumentChecker.notNull(organizationId, "organization");
    _organizationsResource = organizationsResource;
    _urlResourceId = organizationId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the organizations resource.
   *
   * @return the organizations resource, not null
   */
  public DataOrganizationMasterResource getOrganizationsResource() {
    return _organizationsResource;
  }

  /**
   * Gets the organization identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the organization master.
   *
   * @return the organization master, not null
   */
  public OrganizationMaster getMaster() {
    return getOrganizationsResource().getOrganizationMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    OrganizationHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, OrganizationHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    OrganizationHistoryResult result = getMaster().history(request);
    return responseOkFudge(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, OrganizationDocument request) {
    return super.update(uriInfo, request);
  }

  @DELETE
  public void remove() {
    super.remove();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    return super.getVersioned(versionId);
  }


  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") String versionId, List<OrganizationDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<OrganizationDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<OrganizationDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "organizations";
  }

}
