/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;

import com.opengamma.core.security.Security;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;

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
  
}
