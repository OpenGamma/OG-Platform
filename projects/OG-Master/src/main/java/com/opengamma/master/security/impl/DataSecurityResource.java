/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.List;

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

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a security.
 */
public class DataSecurityResource
    extends AbstractDocumentDataResource<SecurityDocument> {

  /**
   * The securities resource.
   */
  private final DataSecurityMasterResource _securitiesResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataSecurityResource() {
    _securitiesResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param securitiesResource  the parent resource, not null
   * @param securityId  the security unique identifier, not null
   */
  public DataSecurityResource(final DataSecurityMasterResource securitiesResource, final ObjectId securityId) {
    ArgumentChecker.notNull(securitiesResource, "securitiesResource");
    ArgumentChecker.notNull(securityId, "security");
    _securitiesResource = securitiesResource;
    _urlResourceId = securityId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the securities resource.
   *
   * @return the securities resource, not null
   */
  public DataSecurityMasterResource getSecuritiesResource() {
    return _securitiesResource;
  }

  /**
   * Gets the security identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the security master.
   *
   * @return the security master, not null
   */
  public SecurityMaster getMaster() {
    return getSecuritiesResource().getSecurityMaster();
  }


  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    SecurityHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, SecurityHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    SecurityHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, SecurityDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<SecurityDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<SecurityDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<SecurityDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "securities";
  }
}
