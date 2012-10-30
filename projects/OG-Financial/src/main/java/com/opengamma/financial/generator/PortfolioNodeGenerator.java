/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.PortfolioNode;

/**
 * Service interface for constructing a random, but reasonable, portfolio node.
 */
public interface PortfolioNodeGenerator {

  /**
   * Creates a new portfolio node object. The implementing class will determine the structure of the portfolio and content of positions.
   * 
   * @return the new node, not null
   */
  PortfolioNode createPortfolioNode();

}
