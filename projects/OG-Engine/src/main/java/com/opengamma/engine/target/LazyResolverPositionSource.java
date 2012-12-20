/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Wraps an existing position source to populate a resolver cache as targets are retrieved.
 */
public class LazyResolverPositionSource implements PositionSource, LazyResolver {

  // TODO: should be package visible

  private final PositionSource _underlying;
  private final LazyResolveContext _context;

  public LazyResolverPositionSource(final PositionSource underlying, final LazyResolveContext context) {
    _underlying = underlying;
    _context = context;
  }

  public/* should be protected */PositionSource getUnderlying() {
    return _underlying;
  }

  @Override
  public LazyResolveContext getLazyResolveContext() {
    return _context;
  }

  @Override
  public void setLazyResolveContext(final LazyResolveContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId) {
    Portfolio portfolio = getUnderlying().getPortfolio(uniqueId);
    if (portfolio == null) {
      return null;
    }
    portfolio = new LazyResolvedPortfolio(getLazyResolveContext(), portfolio);
    getLazyResolveContext().cachePortfolioNode(portfolio.getRootNode());
    return portfolio;
  }

  @Override
  public Portfolio getPortfolio(final ObjectId objectId, final VersionCorrection versionCorrection) {
    Portfolio portfolio = getUnderlying().getPortfolio(objectId, versionCorrection);
    if (portfolio == null) {
      return null;
    }
    portfolio = new LazyResolvedPortfolio(new VersionedLazyResolveContext(getLazyResolveContext(), versionCorrection), portfolio);
    getLazyResolveContext().cachePortfolioNode(portfolio.getRootNode());
    return portfolio;
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId) {
    PortfolioNode portfolioNode = getUnderlying().getPortfolioNode(uniqueId);
    if (portfolioNode == null) {
      return null;
    }
    portfolioNode = new LazyResolvedPortfolioNode(getLazyResolveContext(), portfolioNode);
    getLazyResolveContext().cachePortfolioNode(portfolioNode);
    return portfolioNode;
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    Position position = getUnderlying().getPosition(uniqueId);
    if (position == null) {
      return null;
    }
    position = new LazyResolvedPosition(getLazyResolveContext(), position);
    getLazyResolveContext().cachePosition(position);
    return position;
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    Trade trade = getUnderlying().getTrade(uniqueId);
    if (trade == null) {
      return null;
    }
    trade = new LazyResolvedTrade(getLazyResolveContext(), trade);
    getLazyResolveContext().cacheTrade(trade);
    return trade;
  }

}
