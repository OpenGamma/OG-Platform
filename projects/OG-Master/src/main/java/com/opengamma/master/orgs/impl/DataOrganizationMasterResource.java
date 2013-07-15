/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.change.DataChangeManagerResource;
import com.opengamma.id.ObjectId;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for organizations.
 * <p>
 * The organizations resource receives and processes RESTful calls to the organization master.
 */
@Path("organizationMaster")
public class DataOrganizationMasterResource extends AbstractDataResource {

  /**
   * The organization master.
   */
  private final OrganizationMaster _orgMaster;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataOrganizationMasterResource() {
    _orgMaster = null;
  }
  
  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param organizationMaster  the underlying organization master, not null
   */
  public DataOrganizationMasterResource(final OrganizationMaster organizationMaster) {
    ArgumentChecker.notNull(organizationMaster, "organizationMaster");
    _orgMaster = organizationMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the organization master.
   * 
   * @return the organization master, not null
   */
  public OrganizationMaster getOrganizationMaster() {
    return _orgMaster;
  }
  
  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("organizations")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("organizationSearches")
  public Response search(OrganizationSearchRequest request) {
    OrganizationSearchResult result = getOrganizationMaster().search(request);
    return responseOkFudge(result);
  }

  @POST
  @Path("organizations")
  public Response add(@Context UriInfo uriInfo, OrganizationDocument request) {
    OrganizationDocument result = getOrganizationMaster().add(request);
    URI createdUri = (new DataOrganizationResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(createdUri, result);
  }

  @Path("organizations/{organizationId}")
  public DataOrganizationResource findOrganization(@PathParam("organizationId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataOrganizationResource(this, id);
  }

  //-------------------------------------------------------------------------
  // REVIEW jonathan 2011-12-28 -- to be removed when the change topic name is exposed as part of the component config
  @Path("organizations/changeManager")
  public DataChangeManagerResource getChangeManager() {
    return new DataChangeManagerResource(getOrganizationMaster().changeManager());
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("organizationSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("organizations");
    return bld.build();
  }

}
