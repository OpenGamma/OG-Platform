/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;

/**
 * URIs for web-based portfolios.
 */
public class WebPortfoliosUris {

  /**
   * The data.
   */
  private final WebPortfoliosData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebPortfoliosUris(WebPortfoliosData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return portfolios();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI portfolios() {
    return WebPortfoliosResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI portfolio() {
    return WebPortfolioResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param portfolio  the portfolio, not null
   * @return the URI
   */
  public URI portfolio(final ManageablePortfolio portfolio) {
    return WebPortfolioResource.uri(_data, portfolio.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI node() {
    return WebPortfolioNodeResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param node  the node, not null
   * @return the URI
   */
  public URI node(final ManageablePortfolioNode node) {
    return WebPortfolioNodeResource.uri(_data, node.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI nodePositions() {
    return WebPortfolioNodePositionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param node  the node, not null
   * @return the URI
   */
  public URI nodePositions(final ManageablePortfolioNode node) {
    return WebPortfolioNodePositionsResource.uri(_data, node.getUniqueId());
  }

  /**
   * Gets the URI.
   * @param positionId  the position id, not null
   * @return the URI
   */
  public URI nodePosition(final ObjectIdentifiable positionId) {
    return WebPortfolioNodePositionResource.uri(_data, positionId);
  }

}
