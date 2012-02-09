/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

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
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for an exchange.
 */
public class DataExchangeResource extends AbstractDataResource {

  /**
   * The exchanges resource.
   */
  private final DataExchangeMasterResource _exchangesResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param exchangesResource  the parent resource, not null
   * @param exchangeId  the exchange unique identifier, not null
   */
  public DataExchangeResource(final DataExchangeMasterResource exchangesResource, final ObjectId exchangeId) {
    ArgumentChecker.notNull(exchangesResource, "exchangesResource");
    ArgumentChecker.notNull(exchangeId, "exchange");
    _exchangesResource = exchangesResource;
    _urlResourceId = exchangeId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchanges resource.
   * 
   * @return the exchanges resource, not null
   */
  public DataExchangeMasterResource getExchangesResource() {
    return _exchangesResource;
  }

  /**
   * Gets the exchange identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlExchangeId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange master.
   * 
   * @return the exchange master, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return getExchangesResource().getExchangeMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    ExchangeDocument result = getExchangeMaster().get(getUrlExchangeId(), vc);
    return Response.ok(result).build();
  }

  @POST
  public Response update(@Context UriInfo uriInfo, ExchangeDocument request) {
    if (getUrlExchangeId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ExchangeDocument result = getExchangeMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  @DELETE
  public Response remove() {
    getExchangeMaster().remove(getUrlExchangeId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    ExchangeHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, ExchangeHistoryRequest.class);
    if (getUrlExchangeId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ExchangeHistoryResult result = getExchangeMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlExchangeId().atVersion(versionId);
    ExchangeDocument result = getExchangeMaster().get(uniqueId);
    return Response.ok(result).build();
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, ExchangeDocument request) {
    UniqueId uniqueId = getUrlExchangeId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    ExchangeDocument result = getExchangeMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/exchanges/{exchangeId}");
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
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, ExchangeHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/exchanges/{exchangeId}/versions");
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/exchanges/{exchangeId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
