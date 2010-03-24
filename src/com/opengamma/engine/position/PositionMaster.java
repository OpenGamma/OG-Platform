/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.id.DomainSpecificIdentifier;

/**
 * 
 *
 * @author kirk
 */
public interface PositionMaster {

  /**
   * Obtain the names of all root portfolios.
   * 
   * @return
   */
  Collection<String> getRootPortfolioNames();
  
  Portfolio getRootPortfolio(String portfolioName);
  
  Position getPosition(DomainSpecificIdentifier identityKey);
  
  PortfolioNode getPortfolioNode(DomainSpecificIdentifier identityKey);
}
