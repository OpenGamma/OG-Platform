/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;

/**
 * A position master implementation that caches another.
 */
public class CachingPositionMaster implements PositionMaster {
  
  private static final String PORTFOLIO_CACHE = "portfolio";
  private static final String PORTFOLIONODE_CACHE = "portfolioNode";
  private static final String POSITION_CACHE = "position";
  
  private final PositionMaster _underlying;
  private final CacheManager _cacheManager;
  
  private final Cache _portfolio;
  private final Cache _portfolioNode;
  private final Cache _position;
  
  public CachingPositionMaster(final PositionMaster underlying) {
    this (underlying, EHCacheUtils.createCacheManager());
  }
  
  public CachingPositionMaster(final PositionMaster underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying Position Master");
    ArgumentChecker.notNull(cacheManager, "EH cache manager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, PORTFOLIO_CACHE);
    EHCacheUtils.addCache(cacheManager, PORTFOLIONODE_CACHE);
    EHCacheUtils.addCache(cacheManager, POSITION_CACHE);
    _portfolio = EHCacheUtils.getCacheFromManager(cacheManager, PORTFOLIO_CACHE);
    _portfolioNode = EHCacheUtils.getCacheFromManager(cacheManager, PORTFOLIONODE_CACHE);
    _position = EHCacheUtils.getCacheFromManager(cacheManager, POSITION_CACHE);
  }
  
  public PositionMaster getUnderlying() {
    return _underlying;
  }
  
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier identifier) {
    Element e = _portfolio.get(identifier);
    if (e != null) {
      return (Portfolio) e.getValue();
    } else {
      Portfolio p = getUnderlying().getPortfolio(identifier);
      if (p != null) {
        _portfolio.put(new Element(identifier, p));
      }
      return p;
    }
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return getUnderlying().getPortfolioIds();
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier identifier) {
    Element e = _portfolioNode.get(identifier);
    if (e != null) {
      return (PortfolioNode) e.getValue();
    } else {
      PortfolioNode pn = getUnderlying().getPortfolioNode(identifier);
      if (pn != null) {
        _portfolioNode.put(new Element(identifier, pn));
      }
      return pn;
    }
  }

  @Override
  public Position getPosition(UniqueIdentifier identifier) {
    Element e = _position.get(identifier);
    if (e != null) {
      return (Position) e.getValue();
    } else {
      Position p = getUnderlying().getPosition(identifier);
      if (p != null) {
        _position.put(new Element(identifier, p));
      }
      return p;
    }
  }
  
}