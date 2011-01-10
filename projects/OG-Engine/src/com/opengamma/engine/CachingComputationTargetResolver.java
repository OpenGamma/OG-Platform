/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;

/**
 * 
 */
public interface CachingComputationTargetResolver extends ComputationTargetResolver {

  /**
   * Ensures a collection of positions are cached as computation targets
   * 
   * @param positions  the positions to cache
   */
  void cachePositions(Collection<Position> positions);
  
  /**
   * Ensures a collection of securities are cached as computation targets
   * 
   * @param securities  the securities to cache
   */
  void cacheSecurities(Collection<Security> securities);
  
  /**
   * Ensures the nodes in a portfolio hierarchy are cached as computation targets
   * 
   * @param root  the root node in the hierarchy to cache
   */
  void cachePortfolioNodeHierarchy(PortfolioNode root);
  
  /**
   * Ensure a collection of trades are cached as computation targets
   * 
   * @param trades the trades to cache
   */
  void cacheTrades(Collection<Trade> trades);
}
