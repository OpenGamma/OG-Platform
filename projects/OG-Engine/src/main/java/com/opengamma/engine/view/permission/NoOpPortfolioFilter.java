/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.client.PortfolioFilter;

/**
* A portfolio filter that returns the original portfolio
 * without alteration.
*/
public class NoOpPortfolioFilter implements PortfolioFilter {

  @Override
  public Portfolio generateRestrictedPortfolio(Portfolio portfolio) {
    return portfolio;
  }
}
