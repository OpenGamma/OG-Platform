/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link Resolver} built on a {@link PositionSource}.
 */
public class PositionSourceResolver {

  private final PositionSource _underlying;

  private static class TradeResolver extends PositionSourceResolver implements Resolver<Trade> {

    public TradeResolver(final PositionSource underlying) {
      super(underlying);
    }

    @Override
    public Trade resolve(final UniqueId uniqueId) {
      try {
        return getUnderlying().getTrade(uniqueId);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

  }

  private static class PositionResolver extends PositionSourceResolver implements Resolver<Position> {

    public PositionResolver(final PositionSource underlying) {
      super(underlying);
    }

    @Override
    public Position resolve(final UniqueId uniqueId) {
      try {
        return getUnderlying().getPosition(uniqueId);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

  }

  private static class PortfolioNodeResolver extends PositionSourceResolver implements Resolver<PortfolioNode> {

    public PortfolioNodeResolver(final PositionSource underlying) {
      super(underlying);
    }

    @Override
    public PortfolioNode resolve(final UniqueId uniqueId) {
      try {
        return getUnderlying().getPortfolioNode(uniqueId);
      } catch (DataNotFoundException e) {
        return null;
      }
    }

  }

  public PositionSourceResolver(final PositionSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected PositionSource getUnderlying() {
    return _underlying;
  }

  public Resolver<Trade> trade() {
    return new TradeResolver(getUnderlying());
  }

  public Resolver<Position> position() {
    return new PositionResolver(getUnderlying());
  }

  public Resolver<PortfolioNode> portfolioNode() {
    return new PortfolioNodeResolver(getUnderlying());
  }

}
