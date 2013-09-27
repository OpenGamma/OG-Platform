/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.client.PortfolioFilter;
import com.opengamma.livedata.UserPrincipal;

/**
 * Default portfolio permission provider which implements the
 * ViewPortfolioPermissionProvider interface but does not enforce
 * any permission checks.
 */
public class DefaultViewPortfolioPermissionProvider implements ViewPortfolioPermissionProvider {

  /**
   * No op filter which can be reused by multiple clients.
   */
  public static final NoOpPortfolioFilter PORTFOLIO_FILTER = new NoOpPortfolioFilter();

  /**
   * Create a portfolio filter which will just return the passed portfolio unaltered
   *
   * @param user the user of the portfolio
   * @return the unaltered portfolio
   */
  @Override
  public PortfolioFilter createPortfolioFilter(UserPrincipal user) {
    return PORTFOLIO_FILTER;
  }
}
