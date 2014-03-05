/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a portfolio node.
 */
public class DataPortfolioNodeResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataPortfolioMasterResource _portfoliosResource;
  /**
   * The identifier specified in the URI.
   */
  private UniqueId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param portfoliosResource  the parent resource, not null
   * @param nodeId  the node unique identifier, not null
   */
  public DataPortfolioNodeResource(final DataPortfolioMasterResource portfoliosResource, final UniqueId nodeId) {
    ArgumentChecker.notNull(portfoliosResource, "portfoliosResource");
    ArgumentChecker.notNull(nodeId, "nodeId");
    _portfoliosResource = portfoliosResource;
    _urlResourceId = nodeId;
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
   * Gets the node identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public UniqueId getUrlNodeId() {
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
  public Response get() {
    ManageablePortfolioNode result = getPortfolioMaster().getNode(_urlResourceId);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param nodeId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, UniqueId nodeId) {
    return UriBuilder.fromUri(baseUri).path("/nodes/{nodeId}")
      .build(nodeId);
  }

}
