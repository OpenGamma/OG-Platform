/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

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
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a portfolio.
 */
public class DataPortfolioResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfolioMasterResource _portfoliosResource;
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
  public DataPortfolioResource(final DataPortfolioMasterResource portfoliosResource, final ObjectId portfolioId) {
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
  public DataPortfolioMasterResource getPortfoliosResource() {
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
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    PortfolioDocument result = getPortfolioMaster().get(getUrlPortfolioId(), vc);
    return Response.ok(result).build();
  }

  @POST
  public Response update(@Context UriInfo uriInfo, PortfolioDocument request) {
    if (getUrlPortfolioId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PortfolioDocument result = getPortfolioMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  @DELETE
  public Response remove() {
    getPortfolioMaster().remove(getUrlPortfolioId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    PortfolioHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, PortfolioHistoryRequest.class);
    if (getUrlPortfolioId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PortfolioHistoryResult result = getPortfolioMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlPortfolioId().atVersion(versionId);
    PortfolioDocument result = getPortfolioMaster().get(uniqueId);
    return Response.ok(result).build();
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, PortfolioDocument request) {
    UniqueId uniqueId = getUrlPortfolioId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    PortfolioDocument result = getPortfolioMaster().correct(request);
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}");
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
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, PortfolioHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}/versions");
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
