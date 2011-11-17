/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * RESTful resource for a version of a portfolio.
 */
@Path("/portfolios/{portfolioId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebPortfolioVersionResource extends WebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioVersionResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }
}
