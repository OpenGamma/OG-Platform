/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.net.URI;

import javax.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for an exchange.
 */
@Path("/exgMaster/exchanges/{exchangeId}")
public class DataExchangeResource extends AbstractDataResource {

  /**
   * The exchanges resource.
   */
  private final DataExchangesResource _exchangesResource;
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
  public DataExchangeResource(final DataExchangesResource exchangesResource, final ObjectId exchangeId) {
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
  public DataExchangesResource getExchangesResource() {
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
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    ExchangeDocument result = getExchangeMaster().get(getUrlExchangeId(), VersionCorrection.of(v, c));
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(ExchangeDocument request) {
    if (getUrlExchangeId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ExchangeDocument result = getExchangeMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getExchangeMaster().remove(getUrlExchangeId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ExchangeHistoryRequest request = decodeBean(ExchangeHistoryRequest.class, providers, msgBase64);
    if (getUrlExchangeId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ExchangeHistoryResult result = getExchangeMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    ExchangeDocument result = getExchangeMaster().get(getUrlExchangeId().atVersion(versionId));
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param versionCorrection  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/exchanges/{exchangeId}");
    if (versionCorrection != null && versionCorrection.getVersionAsOf() != null) {
      b.queryParam("versionAsOf", versionCorrection.getVersionAsOf());
    }
    if (versionCorrection != null && versionCorrection.getCorrectedTo() != null) {
      b.queryParam("correctedTo", versionCorrection.getCorrectedTo());
    }
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/exchanges/{exchangeId}/versions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/exchanges/{exchangeId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
