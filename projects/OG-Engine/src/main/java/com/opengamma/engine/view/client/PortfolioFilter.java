/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import com.opengamma.core.position.Portfolio;

/**
 * Responsible for generating a new version of a portfolio, restricted
 * such that only authorised parts are visible. If no restrictions are
 * in place, then the original portfolio may be returned unaltered.
 */
public interface PortfolioFilter {

  /**
   * Generate a new version of the portfolio, with nodes that are not
   * accessible (e.g. due to user permissions) removed.
   *
   * @param portfolio the portfolio to be filtered, not null
   * @return the filtered portfolio which, if no filtering is required,
   * may just be the original
   */
  Portfolio generateRestrictedPortfolio(Portfolio portfolio);
}
