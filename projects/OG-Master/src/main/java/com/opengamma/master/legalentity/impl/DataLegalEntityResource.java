/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

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
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a legalentity.
 */
public class DataLegalEntityResource extends AbstractDocumentDataResource<LegalEntityDocument> {

  /**
   * The legalEntities resource.
   */
  private final DataLegalEntityMasterResource _legalEntitiesResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   */
  DataLegalEntityResource() {
    _legalEntitiesResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param legalEntitiesResource the parent resource, not null
   * @param legalEntityId         the legalentity unique identifier, not null
   */
  public DataLegalEntityResource(final DataLegalEntityMasterResource legalEntitiesResource, final ObjectId legalEntityId) {
    ArgumentChecker.notNull(legalEntitiesResource, "legalEntitiesResource");
    ArgumentChecker.notNull(legalEntityId, "legalentity");
    _legalEntitiesResource = legalEntitiesResource;
    _urlResourceId = legalEntityId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the legalEntities resource.
   *
   * @return the legalEntities resource, not null
   */
  public DataLegalEntityMasterResource getLegalEntitiesResource() {
    return _legalEntitiesResource;
  }

  /**
   * Gets the legalentity identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the legalentity master.
   *
   * @return the legalentity master, not null
   */
  public LegalEntityMaster getMaster() {
    return getLegalEntitiesResource().getLegalEntityMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    LegalEntityHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, LegalEntityHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    LegalEntityHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, LegalEntityDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<LegalEntityDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<LegalEntityDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<LegalEntityDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "legalEntities";
  }

}
