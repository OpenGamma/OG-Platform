/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio.rest;

import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * RESTful resource for a portfolio node.
 */
public class DataSecurePortfolioNodeResource extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataSecurePortfolioMasterResource _portfoliosResource;
  /**
   * The identifier specified in the URI.
   */
  private UniqueId _urlResourceId;

  /**
   * Creates the resource.
   *
   * @param portfoliosResource the parent resource, not null
   * @param nodeId             the node unique identifier, not null
   */
  public DataSecurePortfolioNodeResource(final DataSecurePortfolioMasterResource portfoliosResource, final UniqueId nodeId) {
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
  public DataSecurePortfolioMasterResource getPortfoliosResource() {
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
  public SecurePortfolioMaster getPortfolioMaster() {
    return getPortfoliosResource().getPortfolioMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@HeaderParam("Capability") String portfolioCapabilityStr) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    ManageablePortfolioNode result = getPortfolioMaster().getNode(portfolioCapability, _urlResourceId);
    return responseOkFudge(result);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri the base URI, not null
   * @param nodeId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, UniqueId nodeId) {
    return UriBuilder.fromUri(baseUri).path("/nodes/{nodeId}")
        .build(nodeId);
  }

}
