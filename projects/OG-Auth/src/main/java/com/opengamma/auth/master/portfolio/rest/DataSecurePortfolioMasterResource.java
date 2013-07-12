/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio.rest;

import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.core.change.DataChangeManagerResource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;
import com.sun.jersey.spi.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * RESTful resource for portfolios.
 * <p/>
 * The portfolios resource receives and processes RESTful calls to the portfolio master.
 */
@Path("securePortfolioMaster")
public class DataSecurePortfolioMasterResource extends AbstractDataResource {

  /**
   * The portfolio master.
   */
  private final SecurePortfolioMaster _prtMaster;

  /**
   * Creates dummy resource for the purpose of url resolution.
   */
  public DataSecurePortfolioMasterResource() {
    _prtMaster = null;
  }

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param securePortfolioMaster the underlying portfolio master, not null
   */
  public DataSecurePortfolioMasterResource(final SecurePortfolioMaster securePortfolioMaster) {
    ArgumentChecker.notNull(securePortfolioMaster, "securePortfolioMaster");
    _prtMaster = securePortfolioMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the portfolio master.
   *
   * @return the portfolio master, not null
   */
  public SecurePortfolioMaster getPortfolioMaster() {
    return _prtMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("portfolios")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @POST
  @Path("portfolioSearches")
  public Response search(@HeaderParam("Capability") String portfolioCapabilityStr, PortfolioSearchRequest request) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    PortfolioSearchResult result = getPortfolioMaster().search(portfolioCapability, request);
    return responseOkFudge(result);
  }

  @POST
  @Path("portfolios")
  public Response add(@HeaderParam("Capability") String portfolioCapabilityStr, @Context UriInfo uriInfo, PortfolioDocument request) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    PortfolioDocument result = getPortfolioMaster().add(portfolioCapability, request);
    URI createdUri = (new DataSecurePortfolioResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("nodes/{nodeId}")
  public DataSecurePortfolioNodeResource findPortfolioNode(@PathParam("nodeId") String idStr) {
    UniqueId id = UniqueId.parse(idStr);
    return new DataSecurePortfolioNodeResource(this, id);
  }

  @Path("portfolios/{portfolioId}")
  public DataSecurePortfolioResource findPortfolio(@PathParam("portfolioId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataSecurePortfolioResource(this, id);
  }

  //-------------------------------------------------------------------------
  // REVIEW jonathan 2011-12-28 -- to be removed when the change topic name is exposed as part of the component config
  @Path("portfolios/changeManager")
  public DataChangeManagerResource getChangeManager() {
    return new DataChangeManagerResource(getPortfolioMaster().changeManager());
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("portfolioSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("portfolios");
    return bld.build();
  }

}
