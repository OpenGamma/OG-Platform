/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.engine.target.resolver.Resolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Marker interface for a target resolver that supports lazy operations.
 */
public interface LazyResolver {

  LazyResolveContext getLazyResolveContext();

  void setLazyResolveContext(LazyResolveContext context);

  /**
   * Base class of {@link ObjectResolver} instances that are owned by a parent {@link LazyResolver}.
   */
  public abstract static class ResolverImpl<T extends UniqueIdentifiable> implements ObjectResolver<T> {

    private final LazyResolver _parent;
    private final ObjectResolver<T> _underlying;

    public ResolverImpl(final LazyResolver parent, final ObjectResolver<T> underlying) {
      _parent = parent;
      _underlying = underlying;
    }

    protected ObjectResolver<T> getUnderlying() {
      return _underlying;
    }

    protected abstract T lazy(T object, LazyResolveContext context);

    @Override
    public T resolve(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      final T underlying = _underlying.resolve(uniqueId, versionCorrection);
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

    public LazyPortfolioNodeResolver(final LazyResolver parent, final ObjectResolver<PortfolioNode> underlying) {
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
  public static class LazyPositionResolver extends ResolverImpl<Position> implements IdentifierResolver {

    public LazyPositionResolver(final LazyResolver parent, final Resolver<Position> underlying) {
      super(parent, underlying);
    }

    protected Resolver<Position> getUnderlying() {
      return (Resolver<Position>) super.getUnderlying();
    }

    // ResolverImpl

    @Override
    public Position lazy(final Position object, final LazyResolveContext context) {
      return new LazyResolvedPosition(context, object);
    }

    // IdentifierResolver

    @Override
    public UniqueId resolve(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolve(identifiers, versionCorrection);
    }

    @Override
    public UniqueId resolve(final ObjectId identifier, final VersionCorrection versionCorrection) {
      return getUnderlying().resolve(identifier, versionCorrection);
    }

  }

  /**
   * Lazy resolution of trades.
   */
  public static class LazyTradeResolver extends ResolverImpl<Trade> {

    public LazyTradeResolver(final LazyResolver parent, final ObjectResolver<Trade> underlying) {
      super(parent, underlying);
    }

    @Override
    public Trade lazy(final Trade object, final LazyResolveContext context) {
      return new LazyResolvedTrade(context, object);
    }

  }

}
