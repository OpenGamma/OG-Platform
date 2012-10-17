/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.target.resolver.Resolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Marker interface for a target resolver that supports lazy operations.
 */
public interface LazyResolver {

  LazyResolveContext getLazyResolveContext();

  void setLazyResolveContext(LazyResolveContext context);

  /**
   * Base class of {@link Resolver} instances that are owned by a parent {@link LazyResolver}.
   */
  public abstract static class ResolverImpl<T extends UniqueIdentifiable> implements Resolver<T> {

    private final LazyResolver _parent;
    private final Resolver<T> _underlying;

    public ResolverImpl(final LazyResolver parent, final Resolver<T> underlying) {
      _parent = parent;
      _underlying = underlying;
    }

    protected abstract T lazy(T object, LazyResolveContext context);

    @Override
    public T resolve(final UniqueId uniqueId) {
      final T underlying = _underlying.resolve(uniqueId);
      if (underlying == null) {
        return null;
      }
      return lazy(underlying, _parent.getLazyResolveContext());
    }

  }

  /**
   * Lazy resolution of portfolio nodes.
   */
  public static class LazyPortfolioNodeResolver extends ResolverImpl<PortfolioNode> {

    public LazyPortfolioNodeResolver(final LazyResolver parent, final Resolver<PortfolioNode> underlying) {
      super(parent, underlying);
    }

    @Override
    public PortfolioNode lazy(final PortfolioNode object, final LazyResolveContext context) {
      return new LazyResolvedPortfolioNode(context, object);
    }

  }

  /**
   * Lazy resolution of positions.
   */
  public static class LazyPositionResolver extends ResolverImpl<Position> {

    public LazyPositionResolver(final LazyResolver parent, final Resolver<Position> underlying) {
      super(parent, underlying);
    }

    @Override
    public Position lazy(final Position object, final LazyResolveContext context) {
      return new LazyResolvedPosition(context, object);
    }

  }

  /**
   * Lazy resolution of trades.
   */
  public static class LazyTradeResolver extends ResolverImpl<Trade> {

    public LazyTradeResolver(final LazyResolver parent, final Resolver<Trade> underlying) {
      super(parent, underlying);
    }

    @Override
    public Trade lazy(final Trade object, final LazyResolveContext context) {
      return new LazyResolvedTrade(context, object);
    }

  }

}
