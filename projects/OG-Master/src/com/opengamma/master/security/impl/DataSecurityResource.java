/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a security.
 */
public class DataSecurityResource extends AbstractDataResource {

  /**
   * The securities resource.
   */
  private final DataSecurityMasterResource _securitiesResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

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
  public ObjectId getUrlSecurityId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security master.
   * 
   * @return the security master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return getSecuritiesResource().getSecurityMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    SecurityDocument result = getSecurityMaster().get(getUrlSecurityId(), vc);
    return responseOkFudge(result);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, SecurityDocument request) {
    if (getUrlSecurityId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    SecurityDocument result = getSecurityMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  @DELETE
  public void remove() {
    getSecurityMaster().remove(getUrlSecurityId().atLatestVersion());
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    SecurityHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, SecurityHistoryRequest.class);
    if (getUrlSecurityId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    SecurityHistoryResult result = getSecurityMaster().history(request);
    return responseOkFudge(result);
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlSecurityId().atVersion(versionId);
    SecurityDocument result = getSecurityMaster().get(uniqueId);
    return responseOkFudge(result);
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, SecurityDocument request) {
    UniqueId uniqueId = getUrlSecurityId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    SecurityDocument result = getSecurityMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/securities/{securityId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, SecurityHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/securities/{securityId}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/securities/{securityId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
