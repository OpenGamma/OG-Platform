/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import com.opengamma.core.change.DataChangeManagerResource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationMaster;
import com.opengamma.master.orgs.OrganisationSearchRequest;
import com.opengamma.master.orgs.OrganisationSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * RESTful resource for organisations.
 * <p>
 * The organisations resource receives and processes RESTful calls to the organisation master.
 */
@Path("organisationMaster")
public class DataOrganisationMasterResource extends AbstractDataResource {

  /**
   * The organisation master.
   */
  private final OrganisationMaster _orgMaster;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataOrganisationMasterResource() {
   _orgMaster = null;
  }
  
  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param organisationMaster  the underlying organisation master, not null
   */
  public DataOrganisationMasterResource(final OrganisationMaster organisationMaster) {
    ArgumentChecker.notNull(organisationMaster, "organisationMaster");
    _orgMaster = organisationMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the organisation master.
   * 
   * @return the organisation master, not null
   */
  public OrganisationMaster getOrganisationMaster() {
    return _orgMaster;
  }
  
  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("organisations")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("organisationSearches")
  public Response search(OrganisationSearchRequest request) {
    OrganisationSearchResult result = getOrganisationMaster().search(request);
    return responseOkFudge(result);
  }

  @POST
  @Path("organisations")
  public Response add(@Context UriInfo uriInfo, OrganisationDocument request) {
    OrganisationDocument result = getOrganisationMaster().add(request);
    URI createdUri = (new DataOrganisationResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(createdUri, result);
  }

  @Path("organisations/{organisationId}")
  public DataOrganisationResource findOrganisation(@PathParam("organisationId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataOrganisationResource(this, id);
  }

  //-------------------------------------------------------------------------
  // REVIEW jonathan 2011-12-28 -- to be removed when the change topic name is exposed as part of the component config
  @Path("organisations/changeManager")
  public DataChangeManagerResource getChangeManager() {
    return new DataChangeManagerResource(getOrganisationMaster().changeManager());
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("organisationSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("organisations");
    return bld.build();
  }

}
