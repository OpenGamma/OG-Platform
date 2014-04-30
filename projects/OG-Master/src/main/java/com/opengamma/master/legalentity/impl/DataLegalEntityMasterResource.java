/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

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

import com.opengamma.id.ObjectId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.master.legalentity.LegalEntityMetaDataResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for legalEntities.
 * <p/>
 * The legalEntities resource receives and processes RESTful calls to the legalentity master.
 */
@Path("legalEntityMaster")
public class DataLegalEntityMasterResource extends AbstractDataResource {

  /**
   * The legalentity master.
   */
  private final LegalEntityMaster _exgMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param legalEntityMaster the underlying legal entity master, not null
   */
  public DataLegalEntityMasterResource(final LegalEntityMaster legalEntityMaster) {
    ArgumentChecker.notNull(legalEntityMaster, "legalEntityMaster");
    _exgMaster = legalEntityMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the legalentity master.
   *
   * @return the legalentity master, not null
   */
  public LegalEntityMaster getLegalEntityMaster() {
    return _exgMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("legalentities")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @GET
  @Path("metaData")
  public Response metaData(@Context UriInfo uriInfo) {
    LegalEntityMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, LegalEntityMetaDataRequest.class);
    LegalEntityMetaDataResult result = getLegalEntityMaster().metaData(request);
    return responseOkObject(result);
  }

  @POST
  @Path("legalentitiesearches")
  public Response search(LegalEntitySearchRequest request) {
    LegalEntitySearchResult result = getLegalEntityMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("legalentities")
  public Response add(@Context UriInfo uriInfo, LegalEntityDocument request) {
    LegalEntityDocument result = getLegalEntityMaster().add(request);
    URI createdUri = (new com.opengamma.master.legalentity.impl.DataLegalEntityResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("legalentities/{legalEntityId}")
  public com.opengamma.master.legalentity.impl.DataLegalEntityResource findLegalEntity(@PathParam("legalEntityId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataLegalEntityResource(this, id);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for security meta-data.
   *
   * @param baseUri the base URI, not null
   * @param request the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, LegalEntityMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitiesearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentities");
    return bld.build();
  }

}
