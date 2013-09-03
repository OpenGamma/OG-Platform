/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import java.util.Map;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;

/**
 * Wrapper around a {@link Portfolio} instance that will log any deep resolution calls.
 */
public class LoggedResolutionPortfolio extends AbstractLoggedResolution<Portfolio> implements Portfolio {

  public LoggedResolutionPortfolio(final Portfolio underlying, final ResolutionLogger logger) {
    super(underlying, logger);
  }

  // Portfolio

  @Override
  public Map<String, String> getAttributes() {
    return getUnderlying().getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    getUnderlying().setAttributes(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    getUnderlying().addAttribute(key, value);
  }

  @Override
  public PortfolioNode getRootNode() {
    final PortfolioNode rootNode = getUnderlying().getRootNode();
    // log(ComputationTargetType.PORTFOLIO_NODE, rootNode); // [PLAT-4491] Nodes are linked to portfolio by UID not OID
    return new LoggedResolutionPortfolioNode(rootNode, getLogger());
  }

  @Override
  public String getName() {
    return getUnderlying().getName();
  }

}
