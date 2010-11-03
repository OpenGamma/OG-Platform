/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.rest;

import java.net.URI;

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

import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeHistoryRequest;
import com.opengamma.financial.position.master.PortfolioTreeHistoryResult;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a portfolio tree.
 */
@Path("/data/portfoliotrees/{portfolioId}")
public class DataPortfolioTreeResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfolioTreesResource _portfoliosResource;
  /**
   * The identifier specified in the URI.
   */
  private UniqueIdentifier _urlResourceId;

  /**
   * Creates the resource.
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioId  the portfolio unique identifier, not null
   */
  public DataPortfolioTreeResource(final DataPortfolioTreesResource portfoliosResource, final UniqueIdentifier portfolioId) {
    ArgumentChecker.notNull(portfoliosResource, "position master");
    ArgumentChecker.notNull(portfolioId, "portfolio");
    _portfoliosResource = portfoliosResource;
    _urlResourceId = portfolioId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public DataPortfolioTreesResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the portfolio identifier from the URL.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUrlPortfolioId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getPortfoliosResource().getPositionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get() {
    PortfolioTreeDocument result = getPositionMaster().getPortfolioTree(getUrlPortfolioId());
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(PortfolioTreeDocument request) {
    if (getUrlPortfolioId().equalsIgnoringVersion(request.getPortfolioId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URI");
    }
    PortfolioTreeDocument result = getPositionMaster().updatePortfolioTree(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getPositionMaster().removePortfolioTree(getUrlPortfolioId());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioTreeHistoryRequest request = decodeBean(PortfolioTreeHistoryRequest.class, providers, msgBase64);
    if (getUrlPortfolioId().equalsIgnoringVersion(request.getPortfolioId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URI");
    }
    PortfolioTreeHistoryResult result = getPositionMaster().historyPortfolioTree(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    _urlResourceId = _urlResourceId.withVersion(versionId);
    return get();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * @param baseUri  the base URI, not null
   * @param id  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, UniqueIdentifier id) {
    return UriBuilder.fromUri(baseUri).path("/portfoliotrees/{portfolioId}").build(id.toLatest());
  }

  /**
   * Builds a URI for the versions of the resource.
   * @param baseUri  the base URI, not null
   * @param id  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, UniqueIdentifier id, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfoliotrees/{portfolioId}/versions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build(id.toLatest());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * @param baseUri  the base URI, not null
   * @param uid  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueIdentifier uid) {
    return UriBuilder.fromUri(baseUri).path("/portfoliotrees/{portfolioId}/versions/{versionId}")
      .build(uid.toLatest(), uid.getVersion());
  }

}
