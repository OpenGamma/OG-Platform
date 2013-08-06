/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

import java.net.URI;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.convention.ConventionDocument;
import com.opengamma.financial.convention.ConventionMaster;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a convention.
 */
public class DataConventionResource extends AbstractDataResource {

  /**
   * The configs resource.
   */
  private final DataConventionMasterResource _conventionsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataConventionResource() {
    _conventionsResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param conventionsResource  the parent resource, not null
   * @param configId  the config unique identifier, not null
   */
  public DataConventionResource(final DataConventionMasterResource conventionsResource, final ObjectId configId) {
    ArgumentChecker.notNull(conventionsResource, "conventionsResource");
    ArgumentChecker.notNull(configId, "config");
    _conventionsResource = conventionsResource;
    _urlResourceId = configId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the configs resource.
   *
   * @return the configs resource, not null
   */
  public DataConventionMasterResource getConventionsResource() {
    return _conventionsResource;
  }

  /**
   * Gets the config identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlConventionId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the config master.
   *
   * @return the config master, not null
   */
  public ConventionMaster getConventionMaster() {
    return getConventionsResource().getConventionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    ConventionDocument result = getConventionMaster().get(getUrlConventionId(), vc);
    return responseOkFudge(result);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, ConventionDocument request) {
    if (getUrlConventionId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ConventionDocument result = getConventionMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  @DELETE
  public void remove() {
    getConventionMaster().remove(getUrlConventionId().atLatestVersion());
  }

  ////-------------------------------------------------------------------------
  //@GET
  //@Path("versions")
  //public Response history(@Context UriInfo uriInfo) {
  //  ConfigHistoryRequest<?> request = RestUtils.decodeQueryParams(uriInfo, ConfigHistoryRequest.class);
  //  if (getUrlConventionId().equals(request.getObjectId()) == false) {
  //    throw new IllegalArgumentException("Document objectId does not match URI");
  //  }
  //  ConfigHistoryResult<?> result = getConventionMaster().history(request);
  //  return responseOkFudge(result);
  //}

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlConventionId().atVersion(versionId);
    ConventionDocument result = getConventionMaster().get(uniqueId);
    return responseOkFudge(result);
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, ConventionDocument document) {
    UniqueId uniqueId = getUrlConventionId().atVersion(versionId);
    if (!uniqueId.equals(document.getUniqueId())) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    ConventionDocument result = getConventionMaster().correct(document);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") String versionId, List<ConventionDocument> replacementDocuments) {
    UniqueId uniqueId = getUrlConventionId().atVersion(versionId);

    List<UniqueId> result = getConventionMaster().replaceVersion(uniqueId, replacementDocuments);
    return responseOkFudge(result);
  }

  @PUT
  public <T> Response replaceAllVersions(List<ConventionDocument> replacementDocuments) {
    ObjectId objectId = getUrlConventionId();
    List<UniqueId> result = getConventionMaster().replaceAllVersions(objectId, replacementDocuments);
    return responseOkFudge(result);
  }

  @DELETE
  @Path("versions/{versionId}")
  public void removeVersion(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlConventionId().atVersion(versionId);
    getConventionMaster().removeVersion(uniqueId);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  // TODO replace it with something better
  public static URI uriAll(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/all");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, ConfigHistoryRequest<?> request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/versions");
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
