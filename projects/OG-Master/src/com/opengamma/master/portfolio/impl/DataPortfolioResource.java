/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

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
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for a portfolio.
 */
@Path("/prtMaster/portfolios/{portfolioId}")
public class DataPortfolioResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfoliosResource _portfoliosResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioId  the portfolio unique identifier, not null
   */
  public DataPortfolioResource(final DataPortfoliosResource portfoliosResource, final ObjectId portfolioId) {
    ArgumentChecker.notNull(portfoliosResource, "portfoliosResource");
    ArgumentChecker.notNull(portfolioId, "portfolio");
    _portfoliosResource = portfoliosResource;
    _urlResourceId = portfolioId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * 
   * @return the portfolios resource, not null
   */
  public DataPortfoliosResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the portfolio identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlPortfolioId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * 
   * @return the portfolio master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return getPortfoliosResource().getPortfolioMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    PortfolioDocument result = getPortfolioMaster().get(getUrlPortfolioId(), VersionCorrection.of(v, c));
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(PortfolioDocument request) {
    if (getUrlPortfolioId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PortfolioDocument result = getPortfolioMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getPortfolioMaster().remove(getUrlPortfolioId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioHistoryRequest request = decodeBean(PortfolioHistoryRequest.class, providers, msgBase64);
    if (getUrlPortfolioId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PortfolioHistoryResult result = getPortfolioMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    PortfolioDocument result = getPortfolioMaster().get(getUrlPortfolioId().atVersion(versionId));
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
    // TODO remove this hack
    UriBuilder b = UriBuilder.fromUri(baseUri);
    if (!baseUri.getPath().endsWith("prtMaster/")) {
      b.path("/prtMaster");
    }
    b.path("/portfolios/{portfolioId}");
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
    // TODO remove this hack
    UriBuilder b = UriBuilder.fromUri(baseUri);
    if (!baseUri.getPath().endsWith("prtMaster/")) {
      b.path("/prtMaster");
    }
    b.path("/portfolios/{portfolioId}/versions");
    if (searchMsg != null) {
      b.queryParam("msg", searchMsg);
    }
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    // TODO remove this hack
    UriBuilder b = UriBuilder.fromUri(baseUri);
    if (!baseUri.getPath().endsWith("prtMaster/")) {
      b.path("/prtMaster");
    }
    return b.path("/portfolios/{portfolioId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
