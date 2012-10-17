/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
public interface CachingComputationTargetResolver extends ComputationTargetResolver {

  // [PLAT-444]: move to com.opengamma.engine.target

  /**
   * Hints that a collection of positions should be cached as computation targets.
   * 
   * @param positions the positions to cache
   * @deprecated use {@link #cacheTargets} instead
   */
  @Deprecated
  void cachePositions(Collection<Position> positions);

  /**
   * Hints that a collection of securities should be cached as computation targets
   * 
   * @param securities the securities to cache
   * @deprecated use {@link #cacheTargets} instead
   */
  @Deprecated
  void cacheSecurities(Collection<Security> securities);

  /**
   * Hints that a collection of nodes should be cached as computation targets
   * 
   * @param portfolioNodes the portfolio nodes to cache
   * @deprecated use {@link #cacheTargets} instead
   */
  @Deprecated
  void cachePortfolioNodes(Collection<PortfolioNode> portfolioNodes);

  /**
   * Hints that a collection of trades should be cached as computation targets
   * 
   * @param trades the trades to cache
   * @deprecated use {@link #cacheTargets} instead
   */
  @Deprecated
  void cacheTrades(Collection<Trade> trades);

  /**
   * Hints that a collection of targets should be cached.
   * 
   * @param targets the resolved targets to cache
   */
  void cacheTargets(Collection<? extends UniqueIdentifiable> targets);

}
