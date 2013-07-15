/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationHistoryRequest;
import com.opengamma.master.orgs.OrganisationHistoryResult;
import com.opengamma.master.orgs.OrganisationMaster;
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
 * RESTful resource for a organisation.
 */
public class DataOrganisationResource
    extends AbstractDocumentDataResource<OrganisationDocument> {

  /**
   * The organisations resource.
   */
  private final DataOrganisationMasterResource _organisationsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   */
  DataOrganisationResource() {
    _organisationsResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param organisationsResource the parent resource, not null
   * @param organisationId        the organisation unique identifier, not null
   */
  public DataOrganisationResource(final DataOrganisationMasterResource organisationsResource, final ObjectId organisationId) {
    ArgumentChecker.notNull(organisationsResource, "organisationsResource");
    ArgumentChecker.notNull(organisationId, "organisation");
    _organisationsResource = organisationsResource;
    _urlResourceId = organisationId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the organisations resource.
   *
   * @return the organisations resource, not null
   */
  public DataOrganisationMasterResource getOrganisationsResource() {
    return _organisationsResource;
  }

  /**
   * Gets the organisation identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the organisation master.
   *
   * @return the organisation master, not null
   */
  public OrganisationMaster getMaster() {
    return getOrganisationsResource().getOrganisationMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    OrganisationHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, OrganisationHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    OrganisationHistoryResult result = getMaster().history(request);
    return responseOkFudge(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, OrganisationDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<OrganisationDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<OrganisationDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<OrganisationDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "organisations";
  }

}
