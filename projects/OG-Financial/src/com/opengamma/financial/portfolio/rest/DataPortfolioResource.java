/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.rest;

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

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a portfolio.
 */
@Path("/data/portfolios/{portfolioId}")
public class DataPortfolioResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfoliosResource _portfoliosResource;
  /**
   * The identifier specified in the URI.
   */
  private UniqueIdentifier _urlResourceId;

  /**
   * Creates the resource.
   * @param portfoliosResource  the parent resource, not null
   * @param portfolioId  the portfolio unique identifier, not null
   */
  public DataPortfolioResource(final DataPortfoliosResource portfoliosResource, final UniqueIdentifier portfolioId) {
    ArgumentChecker.notNull(portfoliosResource, "portfoliosResource");
    ArgumentChecker.notNull(portfolioId, "portfolio");
    _portfoliosResource = portfoliosResource;
    _urlResourceId = portfolioId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public DataPortfoliosResource getPortfoliosResource() {
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
   * Gets the portfolio master.
   * @return the portfolio master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return getPortfoliosResource().getPortfolioMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get() {
    PortfolioDocument result = getPortfolioMaster().get(getUrlPortfolioId());
    return Response.ok(result).build();
  }

  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(PortfolioDocument request) {
    if (getUrlPortfolioId().equalsIgnoringVersion(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URI");
    }
    PortfolioDocument result = getPortfolioMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getPortfolioMaster().remove(getUrlPortfolioId());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    PortfolioHistoryRequest request = decodeBean(PortfolioHistoryRequest.class, providers, msgBase64);
    if (getUrlPortfolioId().equalsIgnoringVersion(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document portfolioId does not match URI");
    }
    PortfolioHistoryResult result = getPortfolioMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    _urlResourceId = _urlResourceId.withVersion(versionId);
    return get();
  }

  @GET
  @Path("nodes/{nodeId}")
  public Response getNode(@PathParam("nodeId") String idStr) {
    UniqueIdentifier nodeId = UniqueIdentifier.parse(idStr);
    ManageablePortfolioNode result = getPortfolioMaster().getNode(nodeId);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * @param baseUri  the base URI, not null
   * @param id  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, UniqueIdentifier id) {
    return UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}").build(id.toLatest());
  }

  /**
   * Builds a URI for the versions of the resource.
   * @param baseUri  the base URI, not null
   * @param id  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, UniqueIdentifier id, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}/versions");
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
    return UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}/versions/{versionId}")
      .build(uid.toLatest(), uid.getVersion());
  }

  /**
   * Builds a URI for a specific node.
   * @param baseUri  the base URI, not null
   * @param nodeUid  the resource unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriNode(URI baseUri, UniqueIdentifier nodeUid) {
    return UriBuilder.fromUri(baseUri).path("/portfolios/{portfolioId}/nodes/{nodeId}")
      .build("-", nodeUid);  // TODO: probably could do with a better URI
  }

}
