/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A computation target resolver implementation that caches another implementation.
 */
public class DefaultCachingComputationTargetResolver extends ForwardingComputationTargetResolver implements CachingComputationTargetResolver {

  /** The cache key. */
  private static final String COMPUTATIONTARGET_CACHE = "computationTarget";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  
  /**
   * The cache.
   */
  private final Cache _computationTarget;

  /**
   * Creates an instance using the specified cache manager.
   * @param underlying  the underlying resolver, not null
   * @param cacheManager  the cache manager, not null
   */
  public DefaultCachingComputationTargetResolver(final ComputationTargetResolver underlying, final CacheManager cacheManager) {
    super(underlying);
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, COMPUTATIONTARGET_CACHE);
    _computationTarget = EHCacheUtils.getCacheFromManager(cacheManager, COMPUTATIONTARGET_CACHE);
    if (underlying instanceof DefaultComputationTargetResolver) {
      ((DefaultComputationTargetResolver) underlying).setRecursiveResolver(this);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    switch (specification.getType()) {
      case POSITION :
      case TRADE :
      case PORTFOLIO_NODE :
      case SECURITY :
        final Element e = _computationTarget.get(specification);
        if (e != null) {
          return (ComputationTarget) e.getValue();
        } else {
          final ComputationTarget ct = super.resolve(specification);
          if (ct != null) {
            addToCache(specification, ct);
          }
          return ct;
        }
      default :
        return super.resolve(specification);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void cachePositions(Collection<Position> positions) {
    addToCache(positions, ComputationTargetType.POSITION);
  }
  
  @Override
  public void cacheTrades(Collection<Trade> trades) {
    addToCache(trades, ComputationTargetType.TRADE);
  }
  
  @Override
  public void cacheSecurities(Collection<Security> securities) {
    addToCache(securities, ComputationTargetType.SECURITY);
  }
  
  @Override
  public void cachePortfolioNodeHierarchy(PortfolioNode root) {
    addToCache(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, root.getUniqueId()), new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, root));
    for (PortfolioNode child : root.getChildNodes()) {
      cachePortfolioNodeHierarchy(child);
    }
  }
  
  //-------------------------------------------------------------------------
  private void addToCache(ComputationTargetSpecification specification, ComputationTarget ct) {
    _computationTarget.put(new Element(specification, ct));
  }
  
  private void addToCache(Collection<? extends UniqueIdentifiable> targets, ComputationTargetType targetType) {
    for (UniqueIdentifiable target : targets) {
      addToCache(new ComputationTargetSpecification(targetType, target.getUniqueId()), new ComputationTarget(targetType, target));
    }
  }

  

}
